(ns local-storage.subs
  (:require
   [re-frame.core :as re-frame]))

;; [ views.cljs ] にて、関数「re-frame/subscribe」の引数として呼び出される。
;; 無名関数の引数「db」には、[ db.cljs ] の「default-db」が設定される。
;; 直接 default-db を参照するのではなく、re-frame が管理している状態を参照する。

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::todos
 (fn [db]
   (:todos db)))
