(ns quit-yo-jibber.presence
  (:import [org.jivesoftware.smack RosterListener]
           [org.jivesoftware.smack.util   StringUtils]
           [org.jivesoftware.smack.packet Presence
                                          Presence$Type]))

;; TODO Check presence types match up with :available etc.
(defn mapify-presence [#^Presence m]
  {:from    (StringUtils/parseBareAddress (.getFrom m))
   :jid     (.getFrom m)
   :status  (.getStatus m)
   :type    (keyword (str (.getType m)))
   :mode    (keyword (str (.getMode m)))
   :online? (.isAvailable m)
   :away?   (.isAway m)})

(defn with-presence-map [f]
  (fn [presence] (mapify-presence (f presence))))

(def presence-types {:available   Presence$Type/available
                     :unavailable Presence$Type/unavailable})

(defn set-availability!
  [conn type & [status addr]]
  (let [packet (Presence. (type presence-types))]
    (when status
      (doto packet (.setStatus status)))
    (when addr
      (doto packet (.setTo addr)))
    (doto conn (.sendPacket packet))))

(defn add-presence-listener [conn f]
  (.addRosterListener (.getRoster conn)
                      (proxy [RosterListener] []
                        (entriesAdded [_])
                        (entriesDeleted [_])
                        (entriesUpdated [_])
                        (presenceChanged [presence]
                          (f conn (mapify-presence presence)))))
  conn)

(defn subscribe-presence [conn addr]
  (let [presence (Presence. Presence$Type/subscribe)]
    (.setTo presence addr)
    (doto conn (.sendPacket presence))))


















































