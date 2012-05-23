(ns cadence.model
  (:refer-clojure :exclude [identity])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.validation :as vali]
            [noir.options :as options]
            [cemerick.friend :as friend])
  (:use clojure.walk
        [cemerick.friend.credentials :only [hash-bcrypt]]))

(defn- ensure-indexes []
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
  ([username fields] (mc/find-one-as-map "users" {:username username} fields))
  ([username] (get-user username nil)))

(defn add-user [user]
  (mc/insert "users"
           (assoc (select-keys
                    user
                    (for [[k v] user :when (vali/has-value? v)] k))
                  :password (hash-bcrypt (:password user)))))

(defn add-cadences
  "Batach inserts many cadences for the logged in user."
  [cads]
  true)

(def identity #(get friend/*identity* :current))
(def get-auth #((:authentications friend/*identity*) (:current friend/*identity*)))

(defn get-phrase []
  (if (options/dev-mode?)
    "derp"
    "completing this phrase is fun"))
