package com.sooguosheng.mathplayground;

public class Words {
    private static final String[] ones = {"zero","one","two","three","four","five","six","seven","eight","nine",
            "ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
    private static final String[] tens = {"","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};
    public static String numToWord(int n){
        if (n < 20) return ones[n];
        int t = n/10, u = n%10;
        return u==0 ? tens[t] : tens[t] + "-" + ones[u];
    }
}
