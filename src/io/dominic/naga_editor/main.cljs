(ns io.dominic.naga-editor.main
  (:require
    ["codemirror" :as CodeMirror]
    ["codemirror/mode/clojure/clojure"]
    ["parinfer-codemirror" :as parinferCodeMirror]
    [io.dominic.naga-dagre.core :refer [run-graph!]]
    [naga.rules :as r :refer-macros [r]]
    [naga.engine :as e]
    [asami.core :as mem]
    [cljs.reader :refer [read-string]]))

(def rules
  [(r "shared-parent" [?b :parent ?c] :- [?a :sibling ?b] [?a :parent ?c])
   (r "sibling->brother" [?a :brother ?b] :- [?a :sibling ?b] [?b :gender :male])
   (r "uncle" [?a :uncle ?c] :- [?a :parent ?b] [?b :brother ?c])
   (r "male-father" [?f :gender :male] :- [?a :father ?f])
   (r "female-father" [?f :gender :female] :- [?a :mother ?f])
   (r "parent-father" [?a :parent ?f] :- [?a :father ?f])
   (r "parent-mother" [?a :parent ?f] :- [?a :mother ?f])])

(defn update-graph!
  [cm change-obj]
  (js/window.setTimeout
    (fn []
      (let [axioms
            (try (read-string (.getValue cm))
                 (catch js/Error e
                   nil))
            program (when axioms (r/create-program rules axioms))]
        (when axioms
          (run-graph! (first (e/run {:type :memory} program))
                      (js/document.querySelector "svg#graph")))))
    0))

(def init-axioms
 "[[:fred :sibling :barney]
  [:fred :mother :mary]
  [:mary :sibling :george]
  [:mary :gender :female]
  [:george :gender :male]]")

(defonce cm
  (let [cm
        (CodeMirror
          js/document.body
          #js {:lineNumbers true
               :mode "clojure"
               :value init-axioms})]
    (.init parinferCodeMirror cm "smart" #js {})
    (.on cm "change" update-graph!)
    cm))
