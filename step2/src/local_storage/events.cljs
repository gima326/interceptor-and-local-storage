(ns local-storage.events
  (:require
   [re-frame.core :as re-frame]
   [local-storage.db :as db]
   [cljs.spec.alpha :as s]
   ))

;; -- First Interceptor ------------------------------------------------------
;;
;; Event handlers change state, that's their job. But what happens if there's
;; a bug in the event handler and it corrupts application state in some subtle way?
;; Next, we create an interceptor called `check-spec-interceptor`.
;; Later, we use this interceptor in the interceptor chain of all event handlers.
;; When included in the interceptor chain of an event handler, this interceptor
;; runs `check-and-throw` `after` the event handler has finished, checking
;; the value for `app-db` against a spec.
;; If the event handler corrupted the value for `app-db` an exception will be
;; thrown. This helps us detect event handler bugs early.
;; Because all state is held in `app-db`, we are effectively validating the
;; ENTIRE state of the application after each event handler runs.  All of it.

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (do
    (println "a-spec: " a-spec)
    (println "db: " db)

    (when-not (s/valid? a-spec db)
      (let [error-msg (str "spec check failed: " (s/explain-str a-spec db))
            rslt (assoc db :msg error-msg)]

        (println "error-msg: " error-msg)
        (println "rslt: " rslt)
        rslt))))

;; now we create an interceptor using `after`

(def check-spec-interceptor
  (re-frame/after
   (partial
    check-and-throw
    :local-storage.db/form)))

(def check-interceptor
  (re-frame/after
   ;; after のタイミングで実行する、なんらかの関数を設定しないといけない。
   ;; 引数の有無は問わないみたい。
   (fn [] (println "at check-interceptor"))))

;; -- Second Interceptor -----------------------------------------------------
;;
;; Part of the TodoMVC challenge is to store todos in local storage.
;; Next, we define an interceptor to help with this challenge.
;; This interceptor runs `after` an event handler, and it stores the
;; current todos into local storage.
;; Later, we include this interceptor into the interceptor chain
;; of all event handlers which modify todos.  In this way, we ensure that
;; every change to todos is written to local storage.
(def ->local-store
  (re-frame/after

   ;; after のタイミングで実行する、なんらかの関数を設定しないといけない。
   ;; 引数の有無は問わないみたい。
   db/todos->local-store))


;; -- Interceptor Chain ------------------------------------------------------
;;
;; Each event handler can have its own chain of interceptors.
;; We now create the interceptor chain shared by all event handlers
;; which manipulate todos.
;; A chain of interceptors is a vector of interceptors.
;; Explanation of the `path` Interceptor is given further below.
(def todo-interceptors
  [

   ;; interceptor で spec 的なチェックを実施するのは不向きみたい。
   ;; 画面にエラー内容を表示し、再実施を促したいところだが…。
   ;; 例外を投げて処理を終了することしかできないみたい。
   ;;check-spec-interceptor

   ;; ensure the spec is still valid  (after)
   check-interceptor

   ;; the 1st param given to handler will be the value from this path within db
   ;; ":todos" は、db.cljs、subs.cljs と一致させる必要がある。
   (re-frame/path db/DATA-KEY)

   ;; write todos to localstore  (after)
   ->local-store
   ])

;; -- Event Handlers ----------------------------------------------------------

;; usage:  (dispatch [:initialise-db])
;;
;; This event is dispatched in the app's `main` (core.cljs).
;; It establishes initial application state in `app-db`.
;; That means merging:
;;   1. Any todos stored in LocalStore (from the last session of this app)
;;   2. Default initial values
;;
;; Advanced topic:  we inject the todos currently stored in LocalStore
;; into the first, coeffect parameter via use of the interceptor
;;    `(inject-cofx :local-store-todos)`
;;
;; To fully understand this advanced topic, you'll have to read the tutorials
;; and look at the bottom of `db.cljs` for the `:local-store-todos` cofx
;; registration.
(re-frame/reg-event-fx      ;; part of the re-frame API
 ::initialize-db            ;; event id being handled

 ;; the interceptor chain (a vector of 2 interceptors in this case)
 [
  ;; gets todos from localstore, and puts value into coeffects arg
  (re-frame/inject-cofx ::db/local-store-todos)

  ;; after event handler runs, check app-db for correctness.
  ;; Does it still match Spec?
  check-interceptor
  ;;check-spec-interceptor

  ]
 ;; the event handler (function) being registered

 ;; take 2 values from coeffects. Ignore event vector itself.
 (fn [{:keys [db local-store-todos]} _]

;;   (println "db:" db)
;;   (println "local-store-todos:" local-store-todos)
;;   (println "db/default-db:" db/default-db)

   (let [rslt {:db (assoc db/default-db db/DATA-KEY local-store-todos)}]
     (println "rslt: " rslt)
     rslt
     )))

;; -- Helpers -----------------------------------------------------------------

(defn- allocate-next-id [todos]
  "Returns the next todo id.
  Assumes todos are sorted.
  Returns one more than the current largest id."
  (-> todos
      keys
      last
      ((fnil inc 0))))

(re-frame/reg-event-db
 ::add-todo

 ;; Use the standard interceptors, defined above, which we
 ;; use for all todos-modifying event handlers. Looks after
 ;; writing todos to LocalStore, etc.

 todo-interceptors

 (fn [db [event title]]
   (let [id (allocate-next-id (db/DATA-KEY db))
         new-record {:id id :title title :completed false}]

     (println "db: " db)
     (println "new-record: " new-record)

     ;; この戻り値は、
     ;; 「todo-interceptors」のなかの各インターセプター内で
     ;; 「re-frame/after」に設定された無名関数の引数として設定される。
     (if (s/valid? :local-storage.db/todo new-record)
       ;; valid
       (-> db
           (assoc :msg "")
           (assoc-in [db/DATA-KEY id] new-record))

       ;; invalid
       (-> db
           (assoc :msg (str
                        "spec check failed: "
                        (s/explain-str :local-storage.db/todo new-record))))
       ))))

(re-frame/reg-event-db
 ::delete

 (fn [db [event _]]
   (let [rslt (assoc db db/DATA-KEY {})]

     ;;(println "db:" db)
     ;;(println "event:" event)

     ;; ローカルストレージ消去
     (db/remove-local-storage)

     ;; あわせて db も初期化
     ;; views.cljs の再描画は、「re-frame/subscribe」が担当してくれている。
     rslt)))
