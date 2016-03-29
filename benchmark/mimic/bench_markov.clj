(ns mimic.bench-markov
  (:require [mimic.markov :as markov]
            [criterium.core :as crit]))

;; test sentences courtesy of @realDonaldTrump
(def sentences [
  "Why is this reporter touching me as I leave news conference? What is in her hand??"
  "This was the reporters statement- when she found out there was tape from my facility, she changed her tune."
  "Victory press conference was over. Why is she allowed to grab me and shout questions? Can I press charges?"
  "Why aren't people looking at this reporters earliest statement as to what happened, that is before she found out the episode was on tape?"
  "Wow, Corey Lewandowski, my campaign manager and a very decent man, was just charged with assaulting a reporter. Look at tapes-nothing there!"
  "I have millions more votes/hundreds more dels than Cruz or Kasich, and yet am not being treated properly by the Republican Party or the RNC."
  "We need to secure our borders ASAP. No games, we must be smart, tough and vigilant. MAKE AMERICA GREAT AGAIN & MAKE AMERICA STRONG AGAIN!"
  "Lyin' Ted, I have already beaten you in all debates, and am way ahead of you in votes and delegates. You should focus on jobs & illegal imm!"
  "Lyin' Ted Cruz is weak & losing big, so now he wants to debate again. But, according to Drudge,Time and on-line polls, I have won all debates"
  "After the way I beat Gov. Scott Walker (and Jeb, Rand, Marco and all others) in the Presidential Primaries, no way he would ever endorse me!"
  "Just released that international gangs are all over our cities. This will end when I am President!"
  "Ted Cruz is incensed that I want to refocus NATO on terrorism, as well as current mission, but also want others to PAY FAIR SHARE, a must!"
  "A detainee released from Gitmo has killed an American. When will our so-called 'leaders' ever learn!"
  "The United States cannot continue to make such bad, one-sided trade deals. There are only so many jobs we can give up. No more!"
  "Just to show you how unfair Republican primary politics can be, I won the State of Louisiana and get less delegates than Cruz-Lawsuit coming"
  "Why can't the pundits be honest? Hopefully we are all looking for a strong and great country again. I will make it strong and great!  JOBS!"])

(def split-sentences (for [s sentences] (clojure.string/split s #" ")))
;; generate 10000 sentences
(def markov-input (repeatedly 10000 #(rand-nth split-sentences)))
(defn build-markov []
  (loop [mark {}
         sentence-index 0]
    (let [split-sentence (nth markov-input sentence-index)
          n-mark (markov/build split-sentence mark)]
      (if (= sentence-index (- (count markov-input) 1))
        n-mark
        (recur n-mark (inc sentence-index))))))

(def model (build-markov))
(defn fetch-markov []
  (markov/stream model))

(defn report []
  (println "Build Markov")
  (crit/bench build-markov)
  (println "------------")
  (println "Fetch Markov")
  (crit/bench fetch-markov))
