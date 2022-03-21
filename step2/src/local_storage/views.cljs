(ns local-storage.views
  (:require
   [re-frame.core :as re-frame]
   [local-storage.subs :as subs]


   [local-storage.events :as events]
   [clojure.string :as cstr]
   [reagent.core :as reagent]

   ))

(def KEY-CODE-ENTER 13)

(defn todo-input []
  (let [val (reagent/atom "")]
    (fn []
      [:input {:type        "text"
               :value       @val
               :class       "new-todo"
               :placeholder "What needs to be done?"
               :on-change   #(reset! val (-> % .-target .-value))
               :on-key-down #(if (= (.-which %) KEY-CODE-ENTER)
                               (let [title (-> @val cstr/trim)]

                                 (if (seq title)
                                   (re-frame/dispatch [::events/add-todo title]))

                                 ;; 入力欄を初期化
                                 (reset! val "")))}])))


(defn list-todo-item [todo]
  [:li (:title todo)])

(defn error-message-line [err-msg]
  [:div
   {:class "error"}
   err-msg])

(defn todo-list []
  [:div
   ;; 入力欄
   [todo-input]

   ;; リセットボタン
   [:button {:on-click #(re-frame/dispatch [::events/delete])}
    "Clear"]

   ;; エラーメッセージ
   (error-message-line @(re-frame/subscribe [::subs/msg]))

   [:ul
    (map
     (fn [[idx todo]] ^{:key (:id todo)} [list-todo-item todo])
     @(re-frame/subscribe [::subs/todos]))]])
