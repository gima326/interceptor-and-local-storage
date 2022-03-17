(ns local-storage.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))


;; -- Spec --------------------------------------------------------------------
;;
;; This is a clojure.spec specification for the value in app-db. It is like a
;; Schema. See: http://clojure.org/guides/spec
;;
;; The value in app-db should always match this spec. Only event handlers
;; can change the value in app-db so, after each event handler
;; has run, we re-check app-db for correctness (compliance with the Schema).
;;
;; How is this done? Look in events.cljs and you'll notice that all handlers
;; have an "after" interceptor which does the spec re-check.
;;
;; None of this is strictly necessary. It could be omitted. But we find it
;; good practice.

(s/def ::id int?)
(s/def ::completed boolean?)

(s/def ::title string?)
;;(s/def ::title int?)

(s/def ::todo (s/keys :req-un [::id ::title ::completed]))
(s/def ::todos (s/and
                ;; should use the :kind kw to s/map-of (not supported yet)
                (s/map-of ::id ::todo)
                ;; in this map, each todo is keyed by its :id
                #(instance? PersistentTreeMap %)
                ;; is a sorted-map (not just a map)
                 ))

;;(s/def ::db (s/keys :req-un [::todos ::showing]))
(s/def ::form (s/keys :req-un [::todos]))

(defn foo [args]
  (println "db: " args))


;; -- Default app-db Value  ---------------------------------------------------

(def default-db {:todos (sorted-map)})

;; -- Local Storage  ----------------------------------------------------------
;;
;; Part of the todomvc challenge is to store todos in LocalStorage, and
;; on app startup, reload the todos from when the program was last run.
;; But the challenge stipulates to NOT load the setting for the "showing"
;; filter. Just the todos.
;;

;; localstore key
(def LS-KEY "test-localstore-key")

(defn todos->local-store
  "Puts todos into localStorage"
  [todos]
  (do
    (println "todos->local-store:" todos)

    ;; sorted-map written as an EDN map
    (.setItem js/localStorage LS-KEY (str todos))))

(defn remove-local-storage
  "Removes todos from localStorage"
  []
  (.removeItem js/localStorage LS-KEY))

;; -- cofx Registrations  -----------------------------------------------------

;; Use `reg-cofx` to register a "coeffect handler" which will inject the todos
;; stored in localstore.
;;
;; To see it used, look in `events.cljs` at the event handler for `:initialize-db`.
;; That event handler has the interceptor `(inject-cofx :local-store-todos)`
;; The function registered below will be used to fulfill that request.
;;
;; We must supply a `sorted-map` but in LocalStore it is stored as a `map`.
;;
(re-frame/reg-cofx
 ::local-store-todos
 (fn [cofx _]
   ;; put the localstore todos into the coeffect under :local-store-todos
   (assoc cofx :local-store-todos
          ;; read in todos from localstore, and process into a sorted map
          (into (sorted-map)
                (some->> (.getItem js/localStorage LS-KEY)
                         (cljs.reader/read-string)    ;; EDN map -> map
                         )))))
