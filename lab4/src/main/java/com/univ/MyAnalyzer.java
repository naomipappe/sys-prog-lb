package com.univ;

import JavaTeacherLib.LlkContext;
import JavaTeacherLib.MyLang;
import JavaTeacherLib.Node;

/**
 * Hello world!
 */
public class MyAnalyzer extends MyLang {
    private boolean isInitiated;

    MyAnalyzer(String filepath, int llk) {
        super(filepath, llk);
        isInitiated = false;
    }

    private void init() { //Do necessary assignments and inits before initiating LL(k) strong check
        if (!isInitiated) {
            printTerminals();
            printNonterminals();
            createNonProdRools();
            createNonDosNeterminals();
            LlkContext[] firstContext = firstK();
            setFirstK(firstContext);
            LlkContext[] followContext = followK();
            setFollowK(followContext);
            firstFollowK();
            this.isInitiated = true;
            LlkContext[] firstK = getFirstK();
            LlkContext[] followK = getFollowK();
        }
    }

    public boolean strongLlkCondition() {
        init();
        int leftProductionRuleID = 0;
        for (Node leftProductionRule : this.getLanguarge()) {
            //get context for left rule to compare

            ++leftProductionRuleID;
            int[] leftProductionRuleLexemCodes = leftProductionRule.getRoole();
            LlkContext leftProductionRuleContext = leftProductionRule.getFirstFollowK();

            int rightProductionRuleID = 0;
            for (Node SecondProductionRule : this.getLanguarge()) {
                //get context for right rule to compare

                ++rightProductionRuleID;

                //in case when left and right rules are the same - break and skip to next rule
                if (leftProductionRuleID == rightProductionRuleID) break;

                int[] secondProductionRuleLexemCodes = SecondProductionRule.getRoole();

                if (leftProductionRuleLexemCodes[0] == secondProductionRuleLexemCodes[0]) {
                    // if lexem codes are equal than proceed to analyze rule pair

                    LlkContext secondProductionRuleContext = SecondProductionRule.getFirstFollowK();

                    if (checkRulePair(leftProductionRuleID, leftProductionRuleLexemCodes, leftProductionRuleContext,
                            rightProductionRuleID, secondProductionRuleContext))
                        return false;
                }
            }
        }

        System.out.println("Grammar satisfies strong LL(" + this.getLlkConst() + ") condition");
        return true;
    }

    private boolean checkRulePair(int leftProductionRuleID,
                                  int[] leftProductionRuleLexemCodes,
                                  LlkContext leftProductionRuleContext,
                                  int rightProductionRuleID, LlkContext rightProductionRuleContext) {

        for (int wordNumber = 0; wordNumber < leftProductionRuleContext.calcWords(); ++wordNumber) {
            //check for every word in word set
            //if there is a word from left production rule in word set of second production rule than
            //return true - Grammar  does not satisfy
            if (rightProductionRuleContext.wordInContext(leftProductionRuleContext.getWord(wordNumber))) {
                System.out.println(
                        "Pair " + this.getLexemaText(leftProductionRuleLexemCodes[0]) + "-rules " +
                                "(" + rightProductionRuleID + ", " + leftProductionRuleID + ") " +
                                "does not satisfy strong LL(" + this.getLlkConst() + ") condition");
                System.out.println("Grammar does not satisfy strong LL("+this.getLlkConst()+") condition");
                return true;
            }
        }
        return false;
    }

}
