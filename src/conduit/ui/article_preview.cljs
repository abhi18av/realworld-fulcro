(ns conduit.ui.article-preview
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [conduit.handler.mutations :as mutations]
   [conduit.ui.other :as other]
   [fulcro.client.mutations :as m :refer [defmutation]]
   [fulcro.client.dom :as dom]
   [conduit.ui.routes :as routes]))

(defsc ArticlePreviewMeta [this {:article/keys [author created-at liked-by-count liked-by-me]}
                           {:keys [like unlike]}]
  {:query [:article/id :article/created-at :article/liked-by-count :article/liked-by-me
           {:article/author (prim/get-query other/UserPreview)}]
   :ident [:article/by-id :article/id]}
  (dom/div :.article-meta
    (dom/div {:onClick #(routes/go-to-profile this author)}
      (dom/img {:src (:user/image author)}))
    (dom/div :.info
      (dom/div :.author
        {:onClick #(routes/go-to-profile this author)}
        (:user/name author))
      (dom/span :.date
        (other/js-date->string created-at)))
    (dom/button :.btn.btn-sm.pull-xs-right
      (if liked-by-me
        {:className "btn-primary"
         :onClick #(unlike)}
        {:className "btn-outline-primary"
         :onClick #(like)})
      (dom/i :.ion-heart) " " liked-by-count)))

(def ui-article-preview-meta (prim/factory ArticlePreviewMeta {:keyfn :article/id}))

(defsc ArticlePreview [this {:article/keys [id author-id slug title description] :keys [ph/article]}
                       {:keys [on-delete on-edit]}]
  {:ident [:article/by-id :article/id]
   :query [:article/id :article/author-id :article/slug :article/title :article/description :article/body
           {:ph/article (prim/get-query ArticlePreviewMeta)}]}
  (let [whoami                     (prim/shared this :user/whoami)
        {current-user-id :user/id} whoami]
    (dom/div :.article-preview
      (let [like #(if (number? current-user-id)
                    (prim/transact! this `[(mutations/like {:article/id ~id})])
                    (js/alert "You must log in first"))
            unlike #(prim/transact! this `[(mutations/unlike {:article/id ~id})])]
        (ui-article-preview-meta (prim/computed article {:like like :unlike unlike})))
      (when (= current-user-id author-id)
        (dom/span :.pull-xs-right
          (dom/i :.ion-edit
            {:onClick #(on-edit {:article/id id})} " ")
          (dom/i :.ion-trash-a
            {:onClick #(on-delete {:article/id id})} " ")))
      (dom/div :.preview-link
        (dom/h1 {:onClick #(routes/go-to-article this {:article/id id})}
          title)
        (dom/p description)
        (dom/span "Read more...")))))

(def ui-article-preview (prim/factory ArticlePreview {:keyfn :article/id}))

(defn article-list
  [component articles msg-when-empty]
  (let [edit-article   (fn [{:article/keys [id] :as article}]
                         (routes/edit-article component article))
        delete-article (fn [{:article/keys [id] :as article}]
                         (prim/transact! component `[(mutations/delete-article ~article)]))]
    (dom/div
      (if (seq articles)
        (map (fn [a] (ui-article-preview (prim/computed a {:on-delete delete-article
                                                           :on-edit   edit-article})))
          articles)
        msg-when-empty))))
