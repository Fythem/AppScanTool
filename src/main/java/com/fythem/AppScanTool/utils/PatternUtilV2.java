package com.fythem.AppScanTool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtilV2 {
    Map<String, String> patterns_list = new HashMap<String, String>();

    PatternUtilV2() {
        patterns_list.put("slack_token", "(xox[p|b|o|a]-[0-9]{12}-[0-9]{12}-[0-9]{12}-[a-z0-9]{32})");
        patterns_list.put("mailgun_api", "key-[0-9a-zA-Z]{32}");
        patterns_list.put("picatic_api", "sk_live_[0-9a-z]{32}");
        patterns_list.put("google_api", "AIza[0-9A-Za-z-_]{35}");
        patterns_list.put("amazon_aws_access_key_id", "AKIA[0-9A-Z]{16}");
        patterns_list.put("facebook_access_token", "EAACEdEose0cBA[0-9A-Za-z]+");
        patterns_list.put("paypal_braintree_access_token", "access_token\\$production\\$[0-9a-z]{16}\\$[0-9a-f]{32}");
        patterns_list.put("square_oauth_secret", "sq0csp-[ 0-9A-Za-z\\-_]{43}");
        patterns_list.put("square_access_token", "sq0[a-z]{3}-[0-9A-Za-z\\-_]{22,43}");
        patterns_list.put("stripe_standard_api", "sk_live_[0-9a-zA-Z]{24}");
        patterns_list.put("stripe_restricted_api", "rk_live_[0-9a-zA-Z]{24}");
        patterns_list.put("private_ssh_key", "-----BEGIN PRIVATE KEY-----[a-zA-Z0-9\\S]{100,}-----END PRIVATE KEY-----");
        patterns_list.put("private_rsa_key", "-----BEGIN RSA PRIVATE KEY-----[a-zA-Z0-9\\S]{100,}-----END RSA PRIVATE KEY-----");
        patterns_list.put("gpg_private_key_block", "-----BEGIN PGP PRIVATE KEY BLOCK-----");
        patterns_list.put("link_finder", "((?:https?://|www\\d{0,3}[.])[a-zA-Z0-9_-]+(?:\\.[a-zA-Z0-9_-]+)+[\\w().=/;,#:@?&~*+!$%{}-]*)");
        patterns_list.put("password_in_url", "[a-zA-Z]{3,10}://[^/\\s:@]{3,20}:[^/\\s:@]{3,20}@.{1,100}[\"'\\s]");
    }

    public HashMap<String, ArrayList<String>> start_match(ArrayList<File> files) {
        HashMap<String, ArrayList<String>> sens_info = new HashMap<>();
        for (Map.Entry<String, String> entry : patterns_list.entrySet()) {
            String p_name = entry.getKey();
            String regular = entry.getValue();

            ArrayList<String> match_string = new ArrayList<>();

            for (File file : files) {
                match_single_file(file, regular, match_string);
            }
            sens_info.put(p_name, match_string);
        }
        return sens_info;
    }


    public void match_single_file(File file, String regular, ArrayList match_string) {
        StringBuilder file_string = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String runline;
            while ((runline = reader.readLine()) != null) {
                file_string.append(runline).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern pattern = Pattern.compile(regular);
        Matcher matcher = pattern.matcher(file_string);
        while (matcher.find()) {
            if (matcher.group().contains("http://schemas.android.com/apk/res/android")
                    || matcher.group().contains("http://www.w3.org/2000/svg")
                    || matcher.group().contains("http://www.apple.com/")
                    || matcher.group().contains("https://www.apple.com/")
                    || matcher.group().contains("http://ocsp.apple.com/")
                    || matcher.group().contains("http://crl.apple.com/")
                    || matcher.group().contains("http://certs.apple.com/")
                    || matcher.group().contains("http://schemas.android.com/")
            ) {
                continue;
            }
            match_string.add(matcher.group());
        }
    }
}
