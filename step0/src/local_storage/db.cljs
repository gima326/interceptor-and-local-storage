(ns local-storage.db
  (:require [cljs.reader]
            ;;[cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))

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
