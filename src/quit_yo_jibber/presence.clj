(ns quit-yo-jibber.presence
  (:import [org.jivesoftware.smack.roster RosterListener]
           [org.jivesoftware.smack.util   StringUtils]
           [org.jivesoftware.smack.packet Presence
                                          Presence$Type]))

;; TODO Check presence types match up with :available etc.
(defn mapify-presence [#^Presence m]
  {:from    (.getFrom m)
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

; (defn add-presence-listener [conn f]
;   (.addRosterListener (.getRoster conn)
;                       (proxy [RosterListener] []
;                         (entriesAdded [_])
;                         (entriesDeleted [_])
;                         (entriesUpdated [_])
;                         (presenceChanged [presence]
;                           (f conn (mapify-presence presence)))))
;   conn)

(defn subscribe-presence [conn addr]
  (let [presence (Presence. Presence$Type/subscribe)]
    (.setTo presence addr)
    (doto conn (.sendPacket presence))))


; (defn- all-jids-for-user
;   [conn user]
;   (map (memfn getFrom) (iterator-seq (.getPresences (.getRoster conn) user))))

(def ^:private resource-id {:phone   ["Messaging" "android_talk"]
                            :desktop ["messaging-smgmail" "messaging-AChromeExtension"
                                      "gmail" "BitlBee" "Kopete" "Adium"]})

(defn- resource-type? [conn typeof user]
  (re-matches (re-pattern (str ".*?/(" (clojure.string/join "|" (typeof resource-id)) ").*?")) user))

; (defn all-jids-for-user-of-type [conn typeof user]
;   (seq (filter (partial resource-type? conn typeof) (all-jids-for-user conn user))))
