package com.univ;

import JavaTeacherLib.LlkContext;
import JavaTeacherLib.MyLang;
import JavaTeacherLib.Node;

public class MyAnalyzer extends MyLang {
    private boolean isInitiated;

    MyAnalyzer(String filepath, int llk) {
        super(filepath, llk);
        isInitiated = false;
    }

    private void init() { //Do necessary assignments and init's before initiating LL(k) strong check
        if (!isInitiated) {
            printTerminals();
            printNonterminals();
            createNonProdRools();
            createNonDosNeterminals();
            setFirstK(firstK());
            setFollowK(followK());
            firstFollowK();
            this.isInitiated = true;
        }
    }

    public boolean strongLlkCondition() {
        init();
        int leftRuleID = 0;
        for (Node leftProductionRule : this.getLanguarge()) {


            ++leftRuleID;
            //get lexem codes - essentially a rule structure
            int leftRuleLexemCodes = leftProductionRule.getRoole()[0];
            //get First_k + Follow_k for left rule to compare
            LlkContext leftRuleFirstFollowK = leftProductionRule.getFirstFollowK();


            int rightRuleID = 0;
            for (Node rightProductionRule : this.getLanguarge()) {
                //begin search for a rule that begins with same non terminal
                //не терминал в этом случае - А - то есть левая часть правила
                ++rightRuleID;

                //in case when left and right rules are the same - break and skip to next rule
                if (leftRuleID == rightRuleID) break;

                //get lexem codes - essentially a rule structure
                int rightRuleLexemCodes = rightProductionRule.getRoole()[0];

                if (leftRuleLexemCodes == rightRuleLexemCodes) {
                    // we found a rule that consists of 2 parts
                    //left rule : A->alpha and right rule A->beta

                    //get First_k + Follow_k  for right rule to compare
                    LlkContext rightRuleFirstFollowK = rightProductionRule.getFirstFollowK();

                    if (!checkRulePair(leftRuleFirstFollowK, rightRuleFirstFollowK))
                        System.out.println(
                                "Pair " + this.getLexemaText(leftRuleLexemCodes) + "-rules " +
                                        "(" + rightRuleID + ", " + leftRuleID + ") " +
                                        "does not satisfy strong LL(" + this.getLlkConst() + ") condition");
                    System.out.println("Grammar does not satisfy strong LL("+this.getLlkConst()+") condition");
                        return false;
                }
            }
        }

        System.out.println("Grammar satisfies strong LL(" + this.getLlkConst() + ") condition");
        return true;
    }

    private boolean checkRulePair(LlkContext leftProductionRuleContext, LlkContext rightProductionRuleContext) {

        for (int wordNumber = 0; wordNumber < leftProductionRuleContext.calcWords(); ++wordNumber) {
            // check intersection is empty
            // (First_k(alpha) + Follow_k(A)) intersection (First_k(beta) + Follow_k(A)) = empty set
            // we do this by checking for every word in (First_k(alpha) + Follow_k(A)) that is does not appear in
            // (First_k(beta) + Follow_k(A))
            // if such word exist - return false
            // return false - Grammar  does not satisfy LL(k) strong condition
            if (rightProductionRuleContext.wordInContext(leftProductionRuleContext.getWord(wordNumber))) {
                return false;
            }
        }
        return true;
    }

}
