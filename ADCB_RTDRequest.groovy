import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;





public class RTDRequest {
	
private static Map map1 = Collections.synchronizedMap(new HashMap());

   /* public static Map<String, String> fetchCustomerInfo(String EntityID,Map<String, String> map) throws IOException {
        
	   //	 Properties props = new Properties();
     //   props.load(new FileInputStream("")); // camel-context.properties path

        // SSL Configuration
        //String keyPath = ""; 
        //String keyPass = "changeit";
        //String keyType = "JKS";
        //System.setProperty("javax.net.ssl.keyStore", keyPath);
        //System.setProperty("javax.net.ssl.keyStorePassword", keyPass);
        //System.setProperty("javax.net.ssl.keyStoreType", keyType);
		
		
		//for one way SSL
		//keytool -import -trustcacerts -file server_cert.crt -keystore client_truststore.jks -alias server-cert
		//keytool -import -trustcacerts -file /path/to/your/certificates/server_cert.crt -keystore /path/to/your/client_truststore.jks -alias server-cert

		System.setProperty("javax.net.ssl.trustStore", "path/to/client_truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        // OAuth 2.0 Token Retrieval
       // String clientId = props.getProperty("client_id");
      //  String clientSecret = props.getProperty("client_secret");
	    String clientId ="";
		String clientSecret = "";
        String tokenUrl = "https://devmag.adcb.com/auth/oauth/v2/token";
        
		
		System.out.println("Preparing to hit the token URL: " + tokenUrl);
		
        URL tokenUrlObj = new URL(tokenUrl);
        HttpsURLConnection tokenConnection = (HttpsURLConnection) tokenUrlObj.openConnection();
        tokenConnection.setRequestMethod("POST");
        tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        tokenConnection.setDoOutput(true);



        // Construct the token request body
        String requestBody = "grant_type=client_credentials"+"&client_id=" + clientId +
                             "&client_secret=" + clientSecret +
                             "&scope=CustomerPersonalDetails";
		System.out.println("Token request body prepared: " + requestBody);


        // Send token request
        try (OutputStream os = tokenConnection.getOutputStream()) {
            System.out.println("Sending token request...");
			os.write(requestBody.getBytes("utf-8"));
        }

        // Parse the token response
		System.out.println("Waiting for token response...");
        BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
        StringBuilder tokenContent = new StringBuilder();
        String inputLine;
        while ((inputLine = tokenReader.readLine()) != null) {
            tokenContent.append(inputLine);
        }
        tokenReader.close();

		System.out.println("Token response received: " + tokenContent.toString());
        // Extract the access token from the response JSON
        String accessToken = new JSONObject(tokenContent.toString()).getString("access_token");
        tokenConnection.disconnect();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Failed to obtain access token");
        }
      
	  System.out.println("Access token obtained: " + accessToken);
	  
	  
	   // Make the API call to fetch customer_info
	   
	    String customerId = EntityID;
        String apiUrl = "https://devmag.adcb.com/v2/customer_info?CustomerId=" + customerId;
		
		System.out.println("Preparing to hit the customer info API URL: " + apiUrl);


        URL apiURL = new URL(apiUrl);
        HttpsURLConnection apiConnection = (HttpsURLConnection) apiURL.openConnection();
        apiConnection.setRequestProperty("Content-Type", "application/json");
        apiConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
        apiConnection.setRequestProperty("x-fapi-interaction-id", UUID.randomUUID().toString());
        apiConnection.setRequestMethod("GET");

        System.out.println("Connection established to " + apiUrl);
		
  

        // Reading JSON response
		System.out.println("Reading response from customer info API...");

		
        InputStreamReader reader = new InputStreamReader(apiConnection.getInputStream());
        StringBuilder buf = new StringBuilder();
        char[] cbuf = new char[2048];
        int num;
        while (-1 != (num = reader.read(cbuf))) {
            buf.append(cbuf, 0, num);
        }
        String jsonResponse = buf.toString();
        System.out.println("Response received: " + jsonResponse);

        // Parse JSON response
        // Parse JSON response
		JSONObject jsonObject = new JSONObject(jsonResponse);
		JSONObject data = jsonObject.optJSONObject("Data").optJSONObject("Customer");



         // Parse EmployerInfo
		JSONObject employerInfo = data.optJSONObject("CustomerPersonalDetails").optJSONObject("EmployerInfo");
		map.put("DESIGNATION", employerInfo.optString("Designation", ""));


		// Parse PersonalInfo
		JSONObject personalInfo = data.optJSONObject("CustomerPersonalDetails").optJSONObject("PersonalInfo");
		map.put("GENDER", personalInfo.optString("Gender", ""));
		map.put("NON_RESIDENT_FLAG", personalInfo.optString("ResidencyStatus", ""));
		map.put("CUSTOMER_SINCE", personalInfo.optString("RegistrationDate", ""));
		map.put("MOTHER_MAIDEN_NAME", personalInfo.optString("MotherMaidenName", ""));
		// Parse PersonalInfo
		String fullName = personalInfo.optString("FullName", "");
		map.put("FULL_NAME", fullName);

		if (!fullName.isEmpty()) {
			String[] nameParts = fullName.split("\\s+"); // Split by spaces
			if (nameParts.length >= 3) {
			// Directly assign first, middle, and last name
			map.put("FIRST_NAME", nameParts[0]);
			map.put("MIDDLE_NAME", nameParts[1]);
			map.put("LAST_NAME", nameParts[2]);
		} else if (nameParts.length == 2) {
			// Assign first and last name; no middle name
			map.put("FIRST_NAME", nameParts[0]);
			map.put("MIDDLE_NAME", "");
			map.put("LAST_NAME", nameParts[1]);
		} else if (nameParts.length == 1) {
			// Only first name is present
			map.put("FIRST_NAME", nameParts[0]);
			map.put("MIDDLE_NAME", "");
			map.put("LAST_NAME", "");
		}
		} 
		else {
			// No name provided
			map.put("FIRST_NAME", "");
			map.put("MIDDLE_NAME", "");
			map.put("LAST_NAME", "");
		}
		//map.put("FIRST_NAME", personalInfo.optString("FullName", ""));
		//map.put("MIDDLE_NAME", personalInfo.optString("FullName", ""));
		//map.put("LAST_NAME", personalInfo.optString("FullName", ""));
		map.put("CUST_RM_NAME", personalInfo.optString("RMName", ""));
		map.put("NATIONALITY", personalInfo.optString("NationalityDesc", ""));
		map.put("MARITIAL_STATUS", personalInfo.optString("MaritalStatus", ""));
		map.put("CUSTOMER_TYPE", personalInfo.optString("Category", ""));
		map.put("ROYAL_FLAG", personalInfo.optString("Category", ""));
		map.put("VIP_FLAG", personalInfo.optString("Category", ""));
		map.put("PEP_FLAG", personalInfo.optString("Category", ""));
		map.put("CUSTOMER_SEGMENT", personalInfo.optString("Category", ""));
		map.put("LANGUAGE_PREFERENCE", personalInfo.optString("LanguagePreference", ""));
		map.put("DATE_OF_BIRTH", personalInfo.optString("DateofBirth", ""));

		// Parse KYCInfo
		JSONObject kycInfo = data.optJSONObject("CustomerPersonalDetails").optJSONObject("KYCInfo");
		map.put("PASSPORT_NUMBER", kycInfo.optString("PassportNumber", ""));
		map.put("PASSPORT_EXPIRY_DT", kycInfo.optString("ExpiryDate", ""));
		map.put("VISA_EXPIRY_DT", kycInfo.optString("VisaExpiryDate", ""));
		map.put("EMIRATES_ID_EXPIRY_DT", kycInfo.optString("UAENationalIdExpiryDate", ""));
		map.put("VISA_NUMBER", kycInfo.optString("VisaNumber", ""));
		map.put("TRADE_LICENSE_NUM", kycInfo.optString("TradeLicenseNumber", ""));
		map.put("UAE_NATIONAL_ID", kycInfo.optString("UAENationalId", ""));


       // Prase contact details for contact details fetching
	   JSONObject contactDetails = data.optJSONObject("CustomerPersonalDetails").optJSONObject("ContactDetails");

		map.put("ADDRESS_ZIP", contactDetails.optString("AddressZip", ""));
		map.put("COUNTRY", contactDetails.optString("Country", ""));
		map.put("CITY", contactDetails.optString("City", ""));
		map.put("HOME_LANDLINE_NUMBER", contactDetails.optString("HomeLandlineNumber", ""));
		map.put("ADDRESS_LINE_1", contactDetails.optString("AddressLine1", ""));
		map.put("ADDRESS_LINE_2", contactDetails.optString("AddressLine2", ""));
		map.put("ADDRESS_LINE_3", contactDetails.optString("AddressLine3", ""));
		map.put("MOBILE_NUMBER", contactDetails.optString("MobileNumber", ""));
		map.put("PHONE_NUMBER", contactDetails.optString("PhoneNumber", ""));
		map.put("EMAIL_ID", contactDetails.optString("EmailId", ""));
		map.put("STATE", contactDetails.optString("State", ""));
		map.put("OFFICE_LANDLINE_NUMBER", contactDetails.optString("OfficeLandlineNumber", ""));
		map.put("FAX", contactDetails.optString("Fax", ""));


		// Close the connection and return the map
		apiConnection.disconnect();
		return map;
    }*/

		/*public static Map<String, String> fetchAccountInfo(String EntityID,Map<String, String> map) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(""));

        // SSL Configuration
        String keyPath = "";
        String keyPass = "changeit";
        String keyType = "JKS";
        System.setProperty("javax.net.ssl.keyStore", keyPath);
        System.setProperty("javax.net.ssl.keyStorePassword", keyPass);
        System.setProperty("javax.net.ssl.keyStoreType", keyType);

        // OAuth 2.0 Token Retrieval
       // String clientId = props.getProperty("client_id");
      //  String clientSecret = props.getProperty("client_secret");
	    String clientId ="";
		String clientSecret ="";
        String tokenUrl = "https://devmag.adcb.com/auth/oauth/v2/token";
		
		System.out.println("Preparing to hit the token URL: " + tokenUrl);


        URL tokenUrlObj = new URL(tokenUrl);
        HttpsURLConnection tokenConnection = (HttpsURLConnection) tokenUrlObj.openConnection();
        tokenConnection.setRequestMethod("POST");
        tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        tokenConnection.setDoOutput(true);

        // Construct the token request body
        String requestBody = "grant_type=client_credentials&client_id=" + clientId +
                             "&client_secret=" + clientSecret+
                             "&scope=Accounts";
        
		System.out.println("Token request body prepared: " + requestBody);

		
        // Send token request
        try (OutputStream os = tokenConnection.getOutputStream()) {
            System.out.println("Sending token request...");
            os.write(requestBody.getBytes("utf-8"));
        }

        // Parse the token response
        BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
        StringBuilder tokenContent = new StringBuilder();
        String inputLine;
        while ((inputLine = tokenReader.readLine()) != null) {
            tokenContent.append(inputLine);
        }
        tokenReader.close();
        
		System.out.println("Token response received: " + tokenContent.toString());

        // Extract the access token from the response JSON
        String accessToken = new JSONObject(tokenContent.toString()).getString("access_token");
        tokenConnection.disconnect();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Failed to obtain access token");
        }
          
		  System.out.println("Access token obtained: " + accessToken);

        // Make the API call to fetch account info
		String customerId =  EntityID ;
        String apiUrl = "https://devmag.adcb.com/v2/accounts/inquiry?CustomerId=" + customerId;
        
		System.out.println("Preparing to hit the customer info API URL: " + apiUrl);

		
        URL apiURL = new URL(apiUrl);
        HttpsURLConnection apiConnection = (HttpsURLConnection) apiURL.openConnection();
        apiConnection.setRequestProperty("Content-Type", "application/json");
        apiConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
        apiConnection.setRequestProperty("x-fapi-interaction-id", UUID.randomUUID().toString());
        apiConnection.setRequestMethod("GET");

        System.out.println("Connection established to " + apiUrl);

        // Reading JSON response
		System.out.println("Reading response from customer info API...");

        InputStreamReader reader = new InputStreamReader(apiConnection.getInputStream());
        StringBuilder buf = new StringBuilder();
        char[] cbuf = new char[2048];
        int num;
        while (-1 != (num = reader.read(cbuf))) {
            buf.append(cbuf, 0, num);
        }
        String jsonResponse = buf.toString();
        System.out.println("Response received: " + jsonResponse);

         // Parse JSON response
        JSONObject jsonObject = new JSONObject(jsonResponse);
		JSONObject data = jsonObject.optJSONObject("Data");
        JSONArray accountArray = data.getJSONArray("Account");
		
		   Map<String, Number> map2 = new HashMap<>(); // this map is used to store  all the account id for a customer .
         // Loop through each account object in the array
        for (int i = 0; i < accountArray.length(); i++) {
            JSONObject account = accountArray.optJSONObject(i);
            
			
            // Create variables for each field in the account
            String AccountId = account.optString("AccountId", "");
            String Status = account.optString("Status", "");
            String Currency = account.optString("Currency", "");
            String AccountType = account.optString("AccountType", "");
            String accountSubType = account.optString("AccountSubType", "");
            String productCode = account.optString("ProductCode", "");
            String Description = account.optString("Description", "");
            String OpeningDate = account.optString("OpeningDate", "");
            String maturityDate = account.optString("MaturityDate", "");
            String loanAmount = account.optString("LoanAmount", "");
            String tenor = account.optString("Tenor", "");
            String interestRate = account.optString("InterestRate", "");
            String emiAmount = account.optString("EMIAmount", "");
            String nextEmiDate = account.optString("NextEMIDate", "");
            String numberOfEmiPaid = account.optString("NumberOfEMIPaid", "");
            String outstandingBalance = account.optString("OutstandingBalance", "");
            String repaymentFrequency = account.optString("RepaymentFrequency", "");

            // Store variables in map with unique keys for each account entry
            map.put("Account_" + i + "_AccountId", AccountId);
            map.put("Account_" + i + "_Status", Status);
            map.put("Account_" + i + "_AccountType", AccountType);
            map.put("Account_" + i + "_Description", Description);
            map.put("Account_" + i + "_OpeningDate", OpeningDate);
			map.put("Account_" + i + "_Currency", Currency);
			
			map2.put(AccountId, i);
			
        }
		
		

        apiConnection.disconnect();
        return fetchBalanceInfo(customerId,map,map2);
    }

	public static Map<String, String> fetchBalanceInfo(String EntityID,Map<String, String> map,Map<String, Number> map2) throws IOException {
         Properties props = new Properties();
            props.load(new FileInputStream(" "));  //

            // SSL Configuration
            String keyPath = "";
           String keyPass = "changeit";
           String keyType = "JKS";
           System.setProperty("javax.net.ssl.keyStore", keyPath);
           System.setProperty("javax.net.ssl.keyStorePassword", keyPass);
           System.setProperty("javax.net.ssl.keyStoreType", keyType);

            // OAuth 2.0 Token Retrieval
           // String clientId = props.getProperty("client_id");
          //  String clientSecret = props.getProperty("client_secret");
		  String clientId = "";
		  String clientSecret = "";
            String tokenUrl = "https://devmag.adcb.com/auth/oauth/v2/token";
            System.out.println("Preparing to hit the token URL: " + tokenUrl);
			
            URL tokenUrlObj = new URL(tokenUrl);
            HttpsURLConnection tokenConnection = (HttpsURLConnection) tokenUrlObj.openConnection();
            tokenConnection.setRequestMethod("POST");
            tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            tokenConnection.setDoOutput(true);

            // Construct the token request body
            String requestBody = "grant_type=client_credentials&client_id=" + URLEncoder.encode(clientId) +
                                 "&client_secret=" + URLEncoder.encode(clientSecret) +
                                 "&scope=" + URLEncoder.encode("AccountBalancesDetails", "UTF-8");
                    System.out.println("Token request body prepared: " + requestBody);

            // Send token request
            try (OutputStream os = tokenConnection.getOutputStream()) {
                System.out.println("Sending token request...");
				os.write(requestBody.getBytes("utf-8"));
            }

            // Parse the token response
            BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
            StringBuilder tokenContent = new StringBuilder();
            String inputLine;
            while ((inputLine = tokenReader.readLine()) != null) {
                tokenContent.append(inputLine);
            }
            tokenReader.close();
            System.out.println("Token response received: " + tokenContent.toString());

            // Extract the access token from the response JSON
            String accessToken = new JSONObject(tokenContent.toString()).getString("access_token");
            tokenConnection.disconnect();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new IOException("Failed to obtain access token");
            }
            System.out.println("Access token obtained: " + accessToken);
            // Make the API call to fetch balance info
            String customerId = EntityID ; // Example Customer ID for successful response
            String apiUrl = "https://devmag.adcb.com/v2/balances?CustomerId=" + customerId;
               System.out.println("Preparing to hit the customer info API URL: " + apiUrl);

            URL apiURL = new URL(apiUrl);
            HttpsURLConnection apiConnection = (HttpsURLConnection) apiURL.openConnection();
            apiConnection.setRequestProperty("Content-Type", "application/json");
            apiConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
            apiConnection.setRequestProperty("x-fapi-interaction-id", UUID.randomUUID().toString());
            apiConnection.setRequestMethod("GET");

            System.out.println("Connection established to " + apiUrl);

       // Reading JSON response
	   System.out.println("Reading response from customer info API...");
	InputStreamReader reader = new InputStreamReader(apiConnection.getInputStream());
	StringBuilder buf = new StringBuilder();
	char[] cbuf = new char[2048];
	int num;
	while (-1 != (num = reader.read(cbuf))) {
		buf.append(cbuf, 0, num);
	}
	String jsonResponse = buf.toString();
	System.out.println("Response received: " + jsonResponse);

	// Parse JSON response
	JSONObject jsonObject = new JSONObject(jsonResponse);
	//JSONObject example = jsonObject.optJSONObject("example");
	JSONObject data = jsonObject.optJSONObject("Data");
	JSONArray balanceArray = data.getJSONArray("Balance");


	// Loop through each balance object in the array
	for (int i = 0; i < balanceArray.length(); i++) {
		JSONObject balance = balanceArray.optJSONObject(i);

		// Extracting balance fields
		String accountId = balance.optString("AccountId", "");
		String Amount = "";
		String currency = "";

		// Extract "Amount" and its nested fields
		if (balance.has("Amount") && balance.get("Amount") instanceof JSONObject) {
			JSONObject amountObject = balance.optJSONObject("Amount");
			if (amountObject.has("Amount")) {
				Amount = amountObject.getString("Amount");
			}
			if (amountObject.has("Currency")) {
				currency = amountObject.getString("Currency");
			}
		}
		
		String creditDebitIndicator = balance.optString("CreditDebitIndicator", "");
		String type = balance.optString("Type", "");
		String dateTime = balance.optString("DateTime", "");
		String amountOnHold = balance.optString("AmountOnHold", "");
		String AmountAvailable = balance.optString("AmountAvailable", "");
		 

		// Map these values with unique keys
		map.put("Acct_" + map2.get(accountId) + "_Balance_Amount", Amount);
		map.put("Acct_" + map2.get(accountId) + "_Balance_AmountAvailable", AmountAvailable);

	}
			apiConnection.disconnect();
			return map;
    }
  */

	public static Map<String, String> fetchCustomerInfo(String EntityID, Map<String, String> map) throws IOException {
		if (EntityID == null || EntityID.trim().isEmpty()) {
			throw new IllegalArgumentException("EntityID cannot be null or empty");
		}
		try {
			/*
			// SSL Configuration
			System.setProperty("javax.net.ssl.trustStore", "path/to/client_truststore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

			// OAuth 2.0 Token Retrieval
			String clientId = "fe82e73c-9579-4e8a-ad33-9a3b86eedcc3";
			String clientSecret = "b51c04e5-a205-4598-8138-426eada81e80";
			String tokenUrl = "https://devmag.adcb.com/auth/oauth/v2/token";
			
			System.out.println("Preparing to hit the token URL: " + tokenUrl);
			
			URL tokenUrlObj = new URL(tokenUrl);
			HttpsURLConnection tokenConnection = (HttpsURLConnection) tokenUrlObj.openConnection();
			tokenConnection.setRequestMethod("POST");
			tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			tokenConnection.setDoOutput(true);

			// Updated scope based on the schema
			String requestBody = "grant_type=client_credentials" +
							   "&client_id=" + URLEncoder.encode(clientId, "UTF-8") +
							   "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") +
							   "&scope=AccountDetails AccountBalancesDetails CustomerPersonalDetails";
			
			System.out.println("Token request body prepared: " + requestBody);

			// Send token request
			try (OutputStream os = tokenConnection.getOutputStream()) {
				System.out.println("Sending token request...");
				os.write(requestBody.getBytes(StandardCharsets.UTF_8));
			}

			// Parse the token response
			System.out.println("Waiting for token response...");
			String tokenResponse = "";
			try (BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()))) {
				StringBuilder tokenContent = new StringBuilder();
				String inputLine;
				while ((inputLine = tokenReader.readLine()) != null) {
					tokenContent.append(inputLine);
				}
				tokenResponse = tokenContent.toString();
			}

			System.out.println("Token response received: " + tokenResponse);
			
			JSONObject tokenJson = new JSONObject(tokenResponse);
			String accessToken = tokenJson.optString("access_token");
			if (accessToken == null || accessToken.isEmpty()) {
				throw new IOException("Failed to obtain access token");
			}
			
			tokenConnection.disconnect();
			System.out.println("Access token obtained: " + accessToken);
			
			// Updated API endpoint based on the schema
			//String customerId = URLEncoder.encode(EntityID, "UTF-8");
			String customerId = EntityID;
			String apiUrl = "https://devmag.adcb.com/v2/customer/details?customerId=" + customerId;
			
			System.out.println("Preparing to hit the customer info API URL: " + apiUrl);

			URL apiURL = new URL(apiUrl);
			HttpsURLConnection apiConnection = (HttpsURLConnection) apiURL.openConnection();
			apiConnection.setRequestProperty("Content-Type", "application/json");
			apiConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
			apiConnection.setRequestProperty("x-fapi-interaction-id", UUID.randomUUID().toString());
			apiConnection.setRequestMethod("GET");

			System.out.println("Connection established to " + apiUrl);
			
			String jsonResponse = "";
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(apiConnection.getInputStream()))) {
				StringBuilder responseContent = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					responseContent.append(line);
				}
				jsonResponse = responseContent.toString();
			}*/
			String jsonResponse = "{"
                + "  \"Data\": {"
                + "    \"Customer\": {"
                + "      \"CustomerPersonalDetails\": {"
                + "        \"ContactDetails\": {"
                + "          \"AddressZip\": \"84321\","
                + "          \"Country\": \"UNITED ARAB EMIRATES\","
                + "          \"City\": \"SHARJAH\","
                + "          \"HomeLandlineNumber\": \"023431\","
                + "          \"AddressLine2\": \"FLAT NO.: 204\","
                + "          \"AddressLine1\": \"TEST\","
                + "          \"MobileNumber\": \"971598796532\","
                + "          \"PhoneNumber\": \"023322\","
                + "          \"AddressLine3\": \"Test\","
                + "          \"EmailId\": \"Email@adcb.com\","
                + "          \"State\": \"SHARJAH\","
                + "          \"OfficeLandlineNumber\": \"0233422\","
                + "          \"Fax\": \"97123632\""
                + "        },"
                + "        \"PersonalInfo\": {"
                + "          \"Gender\": \"Male\","
                + "          \"ResidencyStatus\": \"Non-Resident\","
                + "          \"RegistrationDate\": \"2021-05-01\","
                + "          \"MotherMaidenName\": \"Jane Doe\","
                + "          \"FullName\": \"John Michael Smith\","
                + "          \"RMName\": \"Manager Name\","
                + "          \"NationalityDesc\": \"Emirati\","
                + "          \"MaritalStatus\": \"Single\","
                + "          \"Category\": \"Premium\","
                + "          \"LanguagePreference\": \"English\","
                + "          \"DateofBirth\": \"1985-07-15\""
                + "        },"
                + "        \"EmployerInfo\": {"
                + "          \"Designation\": \"Software Engineer\""
                + "        },"
                + "        \"KYCInfo\": {"
                + "          \"PassportNumber\": \"A12345678\","
                + "          \"ExpiryDate\": \"2030-12-31\","
                + "          \"VisaExpiryDate\": \"2025-12-31\","
                + "          \"UAENationalIdExpiryDate\": \"2028-12-31\","
                + "          \"VisaNumber\": \"V12345\","
                + "          \"TradeLicenseNumber\": \"TL12345\","
                + "          \"UAENationalId\": \"UAE123456\""
                + "        }"
                + "      }"
                + "    }"
                + "  }"
                + "}";
			
			System.out.println("Response received: " + jsonResponse);

			// Parse JSON response according to the provided schema
			JSONObject jsonObject = new JSONObject(jsonResponse);
			JSONObject data = jsonObject.optJSONObject("Data");
			if (data == null) {
				throw new IOException("No Data object in response");
			}

			JSONObject customer = data.optJSONObject("Customer");
			if (customer == null) {
				throw new IOException("No Customer object in response");
			}

			JSONObject customerPersonalDetails = customer.optJSONObject("CustomerPersonalDetails");
			if (customerPersonalDetails == null) {
				throw new IOException("No CustomerPersonalDetails in response");
			}

			// Parse Contact Details
			JSONObject contactDetails = customerPersonalDetails.optJSONObject("ContactDetails");
			if (contactDetails != null) {
				map.put("ADDRESS_ZIP", contactDetails.optString("AddressZip", ""));
				map.put("COUNTRY", contactDetails.optString("Country", ""));
				map.put("CITY", contactDetails.optString("City", ""));
				map.put("HOME_LANDLINE_NUMBER", contactDetails.optString("HomeLandlineNumber", ""));
				map.put("ADDRESS_LINE_1", contactDetails.optString("AddressLine1", ""));
				map.put("ADDRESS_LINE_2", contactDetails.optString("AddressLine2", ""));
				map.put("ADDRESS_LINE_3", contactDetails.optString("AddressLine3", ""));
				map.put("MOBILE_NUMBER", contactDetails.optString("MobileNumber", ""));
				map.put("PHONE_NUMBER", contactDetails.optString("PhoneNumber", ""));
				map.put("EMAIL_ID", contactDetails.optString("EmailId", ""));
				map.put("STATE", contactDetails.optString("State", ""));
				map.put("OFFICE_LANDLINE_NUMBER", contactDetails.optString("OfficeLandlineNumber", ""));
				map.put("FAX", contactDetails.optString("Fax", ""));
			}

			// Parse Personal Info
			JSONObject personalInfo = customerPersonalDetails.optJSONObject("PersonalInfo");
			if (personalInfo != null) {
				map.put("GENDER", personalInfo.optString("Gender", ""));
				map.put("NON_RESIDENT_FLAG", personalInfo.optString("ResidencyStatus", ""));
				map.put("CUSTOMER_SINCE", personalInfo.optString("RegistrationDate", ""));
				map.put("MOTHER_MAIDEN_NAME", personalInfo.optString("MotherMaidenName", ""));
				
				String fullName = personalInfo.optString("FullName", "").trim();
				map.put("FULL_NAME", fullName);

				// Process name parts
				if (!fullName.isEmpty()) {
					String[] nameParts = fullName.split("\\s+");
					if (nameParts.length >= 3) {
						map.put("FIRST_NAME", nameParts[0]);
						map.put("MIDDLE_NAME", nameParts[1]);
						map.put("LAST_NAME", nameParts[2]);
					} else if (nameParts.length == 2) {
						map.put("FIRST_NAME", nameParts[0]);
						map.put("MIDDLE_NAME", "");
						map.put("LAST_NAME", nameParts[1]);
					} else if (nameParts.length == 1) {
						map.put("FIRST_NAME", nameParts[0]);
						map.put("MIDDLE_NAME", "");
						map.put("LAST_NAME", "");
					}
				} else {
					map.put("FIRST_NAME", "");
					map.put("MIDDLE_NAME", "");
					map.put("LAST_NAME", "");
				}

				map.put("CUST_RM_NAME", personalInfo.optString("RMName", ""));
				map.put("NATIONALITY", personalInfo.optString("NationalityDesc", ""));
				map.put("MARITIAL_STATUS", personalInfo.optString("MaritalStatus", ""));
				String category = personalInfo.optString("Category", "");
				map.put("CUSTOMER_TYPE", category);
				map.put("ROYAL_FLAG", category);
				map.put("VIP_FLAG", category);
				map.put("PEP_FLAG", category);
				map.put("CUSTOMER_SEGMENT", category);
				map.put("LANGUAGE_PREFERENCE", personalInfo.optString("LanguagePreference", ""));
				map.put("DATE_OF_BIRTH", personalInfo.optString("DateofBirth", ""));
			}

			// Parse Employer Info
			JSONObject employerInfo = customerPersonalDetails.optJSONObject("EmployerInfo");
			if (employerInfo != null) {
				map.put("DESIGNATION", employerInfo.optString("Designation", ""));
			}

			// Parse KYC Info
			JSONObject kycInfo = customerPersonalDetails.optJSONObject("KYCInfo");
			if (kycInfo != null) {
				map.put("PASSPORT_NUMBER", kycInfo.optString("PassportNumber", ""));
				map.put("PASSPORT_EXPIRY_DT", kycInfo.optString("ExpiryDate", ""));
				map.put("VISA_EXPIRY_DT", kycInfo.optString("VisaExpiryDate", ""));
				map.put("EMIRATES_ID_EXPIRY_DT", kycInfo.optString("UAENationalIdExpiryDate", ""));
				map.put("VISA_NUMBER", kycInfo.optString("VisaNumber", ""));
				map.put("TRADE_LICENSE_NUM", kycInfo.optString("TradeLicenseNumber", ""));
				map.put("UAE_NATIONAL_ID", kycInfo.optString("UAENationalId", ""));
			}

			//apiConnection.disconnect();
			return map;
			
		} catch (IOException e) {
			System.err.println("Error in API communication: " + e.getMessage());
			throw e;
		} catch (JSONException e) {
			System.err.println("Error parsing JSON response: " + e.getMessage());
			throw new IOException("Invalid JSON response from API", e);
		}
	}
	
	
	public void prepareADCBResponse(Exchange exchange) {
		String textXml = exchange.getIn().getBody();
		//System.out.println(textXml);
		String textXml1 = textXml.replace('"','/');
		String[] splitXml = textXml1.split("entityType=/", 2);
		String newXml1 = splitXml[1].replaceFirst("/>", "</entityType>");
		String textXml2 = "<entityRequest><entityType>" + newXml1;

		Document doc = convertStringToXMLDocument(textXml2);
		Map map = new HashMap();

		String entityType = null;
		String entityID = null;
		String multiOrg = null;
		String username = null;
		String contactID = null;
		String contactType = null;
		try 
			{
				doc.getDocumentElement().normalize();
					NodeList nList = doc.getElementsByTagName("entityRequest");
					Node nNode = nList.item(0);
					
					Element eElement = (Element) nNode;
					entityType = (eElement.getElementsByTagName("entityType")).item(0).getTextContent();
			        entityID = (eElement.getElementsByTagName("entityID")).item(0).getTextContent();
					multiOrg = (eElement.getElementsByTagName("multiOrg")).item(0).getTextContent();
					username = (eElement.getElementsByTagName("username")).item(0).getTextContent(); /* Release 2 Addition*/
					if((eElement.getElementsByTagName("contactID").item(0)) != null)
					{	contactID = (eElement.getElementsByTagName("contactID")).item(0).getTextContent(); }
					if((eElement.getElementsByTagName("contactType")).item(0) != null)
					{	contactType = (eElement.getElementsByTagName("contactType")).item(0).getTextContent(); }
						
			} catch (Exception e) {
				e.printStackTrace();
		}
		
		switch (entityType)
			{
			/*case "A" : 
				try {
					TimeLimitedCodeBlock.runWithTimeout(new Runnable() {
						@Override
						public void run() {
							try {
								MultifetchAccountInfo m1= new MultifetchAccountInfo(entityID);
								Thread t1= new Thread(m1);
								
								t1.start(); 
								

								t1.join();
							
								//mapThread.putAll(map1);

							}
							catch (InterruptedException e) {
								System.out.println("was interuupted! 1");
							}
						}
					}, 3, TimeUnit.SECONDS);
				}
				catch (TimeoutException e) {
					System.out.println("Got timeout! 2");
				}
				break;    */
			case "X" : 
				try {
					TimeLimitedCodeBlock.runWithTimeout(new Runnable() {
						@Override
						public void run() {
							try {
								MultifetchCustomerInfo m2= new MultifetchCustomerInfo(entityID);
								Thread t2= new Thread(m2);
								
								
				
								t2.start(); 
								t2.join();
								
							
							}
							catch (InterruptedException e) {
								System.out.println("was interuupted! 3");
							}
						}
					}, 3, TimeUnit.SECONDS);
				}
				catch (TimeoutException e) {
					System.out.println("Got timeout! 4");
				}
				break;
				
			/*	case "B" : 
				try {
					TimeLimitedCodeBlock.runWithTimeout(new Runnable() {
						@Override
						public void run() {
							try {
								MultifetchBalanceInfo m3= new MultifetchBalanceInfo();
								Thread t3= new Thread(m3);
								
								
				
								t3.start(); 
								t3.join();
								
							
							}
							catch (InterruptedException e) {
								System.out.println("was interuupted! 5");
							}
						}
					}, 3, TimeUnit.SECONDS);
				}
				catch (TimeoutException e) {
					System.out.println("Got timeout! 6");
				}
				break;
		                  */
}
map.putAll(map1);


	if (contactType == null)
			contactType = "Customer Id Missing";
		if (contactID == null)
			contactID = "Customer Id Missing";

		if (entityType == 'X')
		{
			contactType = entityType;
			contactID = entityID;	
		}
			
		map.put("/entityRequest/entityType", entityType);
		map.put("/entityRequest/entityID", entityID);
		map.put("/entityRequest/multiOrg", multiOrg);
		map.put("/entityRequest/contactID", contactID);
		map.put("/entityRequest/contactType", contactType);
		
		if(responseStatus == "SUCCESS")
			exchange.getIn().setBody(map);

    
}

}




class MultifetchCustomerInfo implements Runnable{  
    
	private String entityID;

	public MultifetchCustomerInfo(String _entityID) {
		this.entityID = _entityID;
	}
	
    @Override
	public void run(){  
		RTDRequest.fetchCustomerInfo(entityID,RTDRequest.map1); 
	}
}

/*class MultifetchAccountInfo implements Runnable{  
	
	private String entityID;

	public MultifetchAccountInfo(String _entityID) {
		this.entityID = _entityID;
	}
	
    @Override
	public void run(){  
		RTDRequest.fetchAccountInfo(entityID,RTDRequest.map1); 
	}
}
*/

class TimeLimitedCodeBlock {

	public static void runWithTimeout(final Runnable runnable, long timeout, TimeUnit timeUnit) throws Exception {
		runWithTimeout(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				runnable.run();
				return null;
			}
		}, timeout, timeUnit);
	}

	public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<T> future = executor.submit(callable);
		executor.shutdown(); // This does not cancel the already-scheduled task.
		try {
			return future.get(timeout, timeUnit);
		}
		catch (TimeoutException e) {
			future.cancel(true);
			throw e;
		}
		catch (ExecutionException e) {
			//unwrap the root cause
			Throwable t = e.getCause();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof Exception) {
				throw (Exception) t;
			} else {
				throw new IllegalStateException(t);
			}
		}
	}

}
