(ns quit-yo-jibber.presence
  (:import [org.jivesoftware.smack RosterListener]
           [org.jivesoftware.smack.packet Presence
                                          Presence$Type]))


(defn mapify-presence [#^Presence m]
  (try
    {:jid     (first (clojure.string/split (.getFrom m) #"/"))
     :status  (.getStatus m)
     :mode    (str (.getMode m))
     :type    (str (.getType m))
     :online? (.isAvailable m)
     :away?   (.isAway m)}
    (catch Exception e (println e) {})))

(defn with-presence-map [f]
  (fn [presence] (mapify-presence (f presence))))

(def presence-types {:available   (Presence. Presence$Type/available)
                     :unavailable (Presence. Presence$Type/unavailable)})

(defn set-availability! [conn type]
  (doto conn (.sendPacket (type presence-types))))

(defn add-presence-listener [conn f]
  (let [roster (.getRoster conn)]
    (doto roster
      (.addRosterListener
       (proxy [RosterListener] []
         (entriesAdded [_])
         (entriesDeleted [_])
         (entriesUpdated [_])
         (presenceChanged [presence]
                          (f (mapify-presence presence))))))))

(defn request-presence [conn addr]
  (let [p (Presence. Presence$Type/subscribe)]
    (doto p (.setTo addr))
    (doto conn (.sendPacket p))))
