courses = [
    "M.Tech (Artificial Intelligence)",
    "M.Tech (Artificial Intelligence)",
    "M.Tech (Artificial Intelligence)",
    "M.Tech (Artificial Intelligence)",
    "M.Tech (Artificial Intelligence)"
]

names = [
    ["Aarav Patel", "Vihaan Sharma", "Aditya Verma", "Sai Iyer", "Arjun Reddy", "Ananya Gupta", "Diya Mehta", "Ishita Singh", "Kavya Joshi", "Mira Malhotra"],
    ["Rohan Chopra", "Aryan Nair", "Kabir Kapoor", "Vivaan Jain", "Reyansh Saxena", "Aditi Agarwal", "Pari Choudhary", "Riya Bhatia", "Saanvi Khatri", "Anika Deshmukh"],
    ["Krishna Kumar", "Ishaan Pandey", "Dhruv Trivedi", "Shaurya Hegde", "Atharva Pillai", "Myra Menon", "Amaira Kaur", "Prisha Rao", "Siya Shetty", "Vani Venkatesh"],
    ["Ayaan Dubey", "Arnav Joshi", "Ayan Thakur", "Kian Tiwari", "Dev Mishra", "Navya Sinha", "Shanaya Chatterjee", "Aadhya Mukherjee", "Kyra Das", "Samaira Bose"],
    ["Advik Ghosh", "Samarth Dutta", "Yug Roy", "Viraj Nanda", "Darsh Sengupta", "Anaisha Biswas", "Fatima Banerji", "Inaya Ganguly", "Kiara Maitra", "Meher Bhowmik"]
]

with open("attendance_data.txt", "w") as f:
    id_counter = 1
    for i in range(len(courses)):
        course = courses[i]
        course_names = names[i]
        for name in course_names:
            id_str = f"STU{id_counter:03d}"
            # ID|Name|Course|0|
            f.write(f"{id_str}|{name}|{course}|0|\n")
            id_counter += 1