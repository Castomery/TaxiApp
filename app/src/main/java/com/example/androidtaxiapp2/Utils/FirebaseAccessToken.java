package com.example.androidtaxiapp2.Utils;

import android.util.Log;

import com.google.common.collect.Lists;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FirebaseAccessToken {

    private static final String firebaseMessagingScope="https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken(){
        try{
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"androidtaxiapp-3a893\",\n" +
                    "  \"private_key_id\": \"4dd149ab19487ac63c51e610153656654d235426\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC3bqNGvtAb0P7F\\nqVDbfggbN7/YVi9YqDl4VXEQfo1NAdkcDWWTWjsaYUFzQaKLhywBrCXvvUjXtEez\\n1me8mW0Iqt9C/+yxA+jHwbDg8pirv7zH0aekSgmS58tzsQRI7hga2XLGKEncrTMQ\\nuMv+w1jZZD96HS4DGDtPpmPqR1xeMDIOe7f0C0USX3k4r8FxYAFe4PdqUh3rHQOY\\n87Ro4daIqhm4nOuadNLVycz1m6aBPOPlV2BLKM77OJdnpTDmz3O/oouDdl6dcHwQ\\nFcORVss+BHf2gmCBdrGsZYgFNAIRZrdSCBjgTZb6lAVDyKcGIWYedrYgYvWPzN27\\nBh4QWIi/AgMBAAECggEABSb+6sBLq9RZgPzUsBhLVam3DPZoGwySnK4ttaxlqf7o\\nyxsXOiXZLiWthyAWAZGjlffGEwSqlKB+48QceCreagAUCFeUSD6XXcZeLQYIwsjp\\nHmW7Az2EelNiHsGfS1sguImuFHcnkqLCIfCcOO+MwdfJLqlo1EkXIBFzkgOSH8py\\nQI5tqCSEVqRGz5yDBe3kGJUUYb78lsr2UCG6FgKwNEwhwoG7OWGCTO4OIxPKa0Pt\\naBXa63BmZNQ9rRk6rE19XET+6EGTCr1pO3uWcKGPoa5+W3dnz/sheLi9Bv3pgef5\\n+xF8/uTqOzIMZq8E7wWDhYzoHMHQPxp4X6hVXH77nQKBgQDip1rWd12fOa6E98Qq\\nQALEOVQi9PqIjU2sTMUyCa46ju4SLj6iMlJ/zfMORTyKhKOMchmQtzSUbO1akHuC\\nW7SKjzmTIlI5LsX2NjDrzqMYCSDA5eniJajU/gfnogpnPPiwJlwT8g89N/D7WCPj\\nIGAnhzXAe++u6+YzSQzB3VR0JQKBgQDPLqpF4kQfF1kpq8uKPpxZSdKB+CFS4GMb\\nz1zzYKWXb0Tu8+AY5ym3TodCLpXVN34DnSg1VZt7BzG9wVG3+OwvShaGZt5it6L+\\nb/kEsf2T7dTyAwXyfudFiQsUGxZv0E5t1631EfMwvkOCWALrtiGuh/1Nw+PN3yj1\\nMwpY0NsiEwKBgQChaS13rmouCpYOe6Rnsk5GD82rV2tT881s9v84M0ywxzkkGWrh\\nd5TpD3IYp5YFz4/pZRhvxwja6vfdlydeHpDkXDtUXQzRTE4OmWkdhXsAkv+QYCu/\\nsbIafQ6EFxHxk6pHSAoq6ieQIVasqIhmJ5hSjifpfFmk6Lp3q346UbTjQQKBgDVw\\nS+sKmZeLZiwha7aUv68oI2vynGXMCsQ5kDgWTZ/py1X3AW5q8Y+eabTgXzVHwvEb\\nsCtRkXECQgo3uuRBCGqKKZUxI1tDEn+eUhSr9EQbrDlgHkCWCwEcgAcHlZnxmMks\\nQGpd3uRLrw4HtXkm2TkzSlqslC2dyeq0545RELSRAoGBAJlLiLHp1oUYJSa5sovO\\ntSskrj6+HbaduL9m+TPec1SIAIlMEZIim5cDdIpCHvXcgmbAd37XTJ+6BiUQm4P4\\nm3LcKJQDO5S+B/VzRCzB8tJc/6SpGE5UYB/UMcAe6o8ybBPgIJmMlhbvpyiQ225r\\nxJM0scB42KbCINOmnRcVGpHp\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-b1b1f@androidtaxiapp-3a893.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"106239671878062172647\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-b1b1f%40androidtaxiapp-3a893.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).createScoped(Lists.newArrayList(firebaseMessagingScope));

            googleCredentials.refresh();

            return googleCredentials.getAccessToken().getTokenValue();

        }catch (IOException e){
            Log.d("ERORRE", e.getMessage());
        }
        return null;
    }
}
