static void readExcel(String filePath) throws Exception {

		FileInputStream file = new FileInputStream(new File(filePath));
		Workbook workbook = new XSSFWorkbook(file);
		Sheet sheet = workbook.getSheetAt(0);
		Map<String, UserPayload> userMap = new LinkedHashMap<>();
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {

			Row row = sheet.getRow(i);
			if (row == null)
				continue;

			String emailsRaw = getCell(row, 0);
			String fullName = getCell(row, 1);
			String groupName = getCell(row, 5);

			groupSet.add(groupName);

			String[] nameParts = fullName.trim().split("\\s+");
			String firstName = nameParts.length > 0 ? nameParts[0] : "";
			String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

			String[] emails = emailsRaw.split("[\\n,]+");

			for (String email : emails) {

				String key = email.trim().toLowerCase();

				UserPayload existing = userMap.get(key);

				if (existing == null) {

					existing = new UserPayload();
					existing.email = key;
					existing.userName = key;
					existing.firstName = firstName;
					existing.lastName = lastName;

					userMap.put(key, existing);
				}

				if (!existing.groupName.contains(groupName)) {
					existing.groupName.add(groupName);
				}

			}
		}
//	        for (UserPayload user : users) {
//	            System.out.println(user.email + " | " + user.firstName + " | " + user.lastName + " | " + user.groupName);
//	        }
//	        
//	        System.out.println(groupSet);

		workbook.close();
		users.clear();
		users.addAll(userMap.values());
	}



	static String getToken() throws Exception {

		URL url = new URL(baseUrl + "/authentication/login");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setDoOutput(true);

		String body = "{" + "\"username\":\"" + username + "\"," + "\"password\":\"" + password + "\"" + "}";

		write(con, body);

		String response = read(con);

		if (!response.contains("access_token")) {
			throw new RuntimeException("Login failed: " + response);
		}

		return response.split("\"access_token\":\"")[1].split("\"")[0];
	}