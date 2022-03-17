(ns local-storage.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [local-storage.events :as events]
   [local-storage.views :as views]
   [local-storage.config :as config]
   ))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)   ;; so that println writes to `console.log`
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/todo-list] root-el)
    ))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
