package ticket.booking.util;

import ticket.booking.util.UserServiceUtil;

public class GenerateHash {
    public static void main(String[] args) {

        String hash =
                UserServiceUtil.hashPassword("12345");

        System.out.println(hash);
    }
}