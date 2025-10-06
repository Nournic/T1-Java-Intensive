package ru.t1.nour.microservice.util.generators;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;
import java.util.StringJoiner;

public class UserScriptGenerator {

    private PasswordEncoder encoder;
    public UserScriptGenerator(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    private static final String[] USERS = {
            "Valerie","Mark","Sandra","Hudson","Hill","Dorothy","Marie","Bishop","Elliott"
    };
    private static final String[] EMAILS = {
            "gmail.com","mail.ru","bk.ru","list.ru"
    };
    private static final String[] PASSWORDS = {
            "UoQfovy^AMiduEf0Udt0", "61mAB1O)fJrF9!d%4MvG", "JO%F6PNsd4$7liJhw%gl",
            "@iYwA9&eVTYFAEjf$IB#", "qW*TvNu$ebY^@8YWOSN3", "fz8i)3II0D(xNO!3MTjB",
            "LE+mP2nnu1GsAKljX+FI", "pztP#nP)Wm$TSHV8AQ2v", "#6Kb42RL7C7$+tBiL8v8"
    };
    public String generateUsersScripts(){
        StringJoiner s = new StringJoiner("\n");

        Random random = new Random();
        for(int i = 0; i<USERS.length; i++)
            s.add("insert into users (id, login, email, password) values (" + (i+1) + ",'" + USERS[i] + "','" + (USERS[i].toLowerCase() + "@" + EMAILS[random.nextInt(4)]) + "','" + encoder.encode(PASSWORDS[i]) + "');");

        return s.toString();
    }
}
