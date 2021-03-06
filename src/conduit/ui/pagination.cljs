(ns conduit.ui.pagination
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.client.dom :as dom]
   [conduit.ui.article-preview :as preview]
   [fulcro.client.routing :as r]
   [conduit.util :as util]))

(defn page-ident [props]
  [:pagination/page
   (select-keys props [:pagination/list-type :pagination/list-id :pagination/size
                       (if (number? (:pagination/end props))
                         :pagination/end
                         :pagination/start)])])

(defsc Page [this {:pagination/keys [list-type list-id size start end previous-id next-id items] :as props}
             {:keys [load-page]}]
  {:ident         (fn [] (page-ident props))
   :initial-state (fn [params]
                    #:pagination{:list-type   :articles/by-feed
                                 :list-id     :global
                                 :size        5
                                 :start       :empty
                                 :previous-id nil
                                 :next-id     nil
                                 :items       (prim/get-initial-state preview/ArticlePreview {})})
   :query         [:pagination/list-type :pagination/list-id :pagination/size
                   :pagination/start :pagination/end
                   :pagination/next-id  :pagination/previous-id
                   {:pagination/items (prim/get-query preview/ArticlePreview)}]}
  (dom/div
    (preview/article-list this
      (if (number? end) (reverse items) items)
      (cond
        (and (= list-type :articles/by-feed) (= list-id :personal))
        "You have no article!"

        :default
        "No article!"))
    (dom/div
      (dom/button :.btn.btn-sm
        (if previous-id
          {:onClick #(load-page previous-id) :className "action-btn btn-outline-primary"}
          {:className "btn-outline-secondary"})
        "Previous")
      (dom/button :.btn.btn-sm
        (if next-id
          {:onClick #(load-page next-id) :className "action-btn btn-outline-primary"}
          {:className "btn-outline-secondary"})
        "Next"))))

(def ui-page (prim/factory Page))

(r/defrouter PageRouter :router/page
  (fn [this props] (page-ident props))
  :pagination/page Page)

(def ui-page-router (prim/factory PageRouter))
