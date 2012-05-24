(ns cadence.model
  (:refer-clojure :exclude [identity])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as vali]
            [cadence.pattern-recognition :as patrec]
            [noir.options :as options]
            [cemerick.friend :as friend])
  (:use clojure.walk
        monger.operators
        [cemerick.friend.credentials :only [hash-bcrypt]])
  (:import [org.bson.types ObjectId]))

(defn- ensure-indexes []
  (mc/ensure-index "cadences" {:random_point "2d"})
  (mc/ensure-index "phrases" {:random_point "2d"})
  (mc/ensure-index "phrases" {:users 1})
  (mc/ensure-index "users" {:username 1} {:unique 1 :dropDups 1}))

(defn connect [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (mg/connect!))
  (let [db-name (:db-name connection-info)]
    (mg/authenticate db-name
                     (:username connection-info)
                     (into-array Character/TYPE (:password connection-info)))
    (mg/set-db! (mg/get-db db-name))
    (ensure-indexes)))

(defn get-user
  ([username fields] (let [get-f
                           (partial
                             mc/find-one-as-map "users" {:username username})]
                       (if (nil? fields)
                         (get-f)
                         (get-f fields))))
  ([username] (get-user username nil)))

(defn add-user [user]
  (mc/insert "users"
           (assoc (select-keys
                    user
                    (for [[k v] user :when (vali/has-value? v)] k))
                  :password (hash-bcrypt (:password user)))))

(defn add-cadences
  "Batch inserts many cadences for the given user."
  [user-id phrase-id cads]
  (mc/insert-batch "cadences"
                   (map (fn [x]
                          (merge x {:user_id user-id
                                    :phrase_id phrase-id
                                    :random_point [(rand) 0]}))
                        cads)))

(defn add-phrases
  "Batch inserts phrases to be used for training and auth."
  [phrases]
  (mc/insert-batch "phrases"
                   (map (fn [x]
                          {:phrase x
                           :users []
                           :random_point [(rand) 0]}) phrases)))

(def identity #(get friend/*identity* :current))
(def get-auth #((:authentications friend/*identity*) (:current friend/*identity*)))

(defn get-phrase [user-id for-auth?]
  (if (options/dev-mode?)
    {:_id (ObjectId. "1234")
     :phrase "derp"
     :users [(:_id (get-auth))]}
    (mc/find-one-as-map "phrases"
                        (if for-auth?
                          {:users user-id :random_point {"$near" (rand)}}
                          {:users {$ne user-id} :random_point {"$near" (rand)}})
                        {:phrase 1})))
