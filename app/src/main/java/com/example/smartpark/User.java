package com.example.smartpark;

public class User {

        private String initials;
        private String name;
        private String plan;

        public User(String initials, String name, String plan) {
            this.initials = initials;
            this.name = name;
            this.plan = plan;
        }

        public String getInitials() { return initials; }
        public String getName() { return name; }
        public String getPlan() { return plan; }
    }

