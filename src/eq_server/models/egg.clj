(ns eq-server.models.egg
  (:require [eq-server.db :as db]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s]))

(defn- award-egg!
  "Awards an egg to the user that found it"
  [egg-id user-id]
  (j/update! (db/db-connection)
             :eggs
             {:user_id user-id}
             (s/where {:id egg-id})))

(defn- update-user-score!
  "Updates the score of a user"
  [user-id new-total-score]
  (j/update! (db/db-connection)
             :users
             {:score new-total-score}
             (s/where {:id user-id})))

(defn find-awardable-eggs-by-distance
  "Finds the nearest eggs within the specified max distance.
   Returns an array of maps, each representing an egg, in distance ascending order"
  [lat lng max-distance limit]
  (j/with-connection (db/db-connection)
    (let [peek-point (str "POINT(" lng " " lat ")")]
      (j/with-query-results res
        [(str "SELECT eggs.*, (ST_Distance(eggs.point, ?::geometry)) AS distance, "
              "egg_types.name AS type_name, egg_types.description AS type_description, "
              "egg_type_modifiers.find_points AS points "
              "FROM eggs, egg_types, egg_type_modifiers "
              "WHERE eggs.egg_type_id=egg_types.id "
              "AND egg_types.id=egg_type_modifiers.egg_type_id "
              "AND ST_Distance(eggs.point, ?::geometry) <= ? "
              "AND eggs.user_id IS NULL "
              "ORDER BY distance ASC "
              (if limit "LIMIT ? " ""))
         peek-point peek-point max-distance (if limit limit)]
        (doall res)))))

(defn award-eggs!
  [eggs user]
  (let [user-id (:id user)
        egg-ids (map #(:id %) eggs)
        egg-scores (map #(:points %) eggs)
        score-sum (+ (:score user) (reduce + egg-scores))]
    (do
      (dorun (map #(award-egg! % user-id) egg-ids))
      (update-user-score! user-id score-sum))))
