package sa.tamkeentech.tbs.service.util;

import org.springframework.core.env.Environment;
import sa.tamkeentech.tbs.config.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

/**
 *
 */
public class CommonUtils {

    private CommonUtils(){}

    public static boolean isProdOrStaging(Environment environment){
        return  Arrays.asList(environment.getActiveProfiles())
                .stream().anyMatch(profile-> Arrays.asList("prod","staging").contains(profile));
    }

    public static boolean isProfile(Environment environment, String profileName){
        return  Arrays.asList(environment.getActiveProfiles())
                .stream().anyMatch(profile-> Arrays.asList(profileName).contains(profile));
    }

	public static String getProfile(Environment environment) {
		String[] profiles = environment.getActiveProfiles();
		return (profiles != null && profiles.length > 0) ?
				environment.getActiveProfiles()[0] : "";
	}

	/*
     *  A convenience method to add a specified number of minutes to a Date object
     *  @param  minutes  The number of minutes to add
     *  @param  beforeTime  The time that will have minutes added to it
     *  @return  A date object with the specified number of minutes added to it
     */
	public static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000; //millisecs
        Date afterAddingMins = new Date(beforeTime.getTime() + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    /**
     * A convenience method to exclude vat
     * @param amount
     * @param vat
     * @return
     */
    public static BigDecimal vatExclude(BigDecimal amount, BigDecimal vat) {
	    if (amount == null || amount.equals(BigDecimal.ZERO)) {
	        return BigDecimal.ZERO;
        }
	    return amount.divide(BigDecimal.ONE.add(vat),6, RoundingMode.HALF_UP);
    }

    /**
     * A helper method to validate a value is within a range
     * @param value
     * @param start
     * @param end
     * @param <T>
     * @return true if value is between start and end (inclusive)
     */
    public static <T extends Comparable<T>> boolean isBetween(T value, T start, T end) {
        return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

    public static ZonedDateTime getLocalDate(ZonedDateTime dateTime, String offset) {
        if (dateTime == null) {
            return dateTime;
        }
        return dateTime.withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(offset)));
    }

    public static ZonedDateTime addSecondsToDate(int seconds, ZonedDateTime beforeTime){
        return beforeTime.plusSeconds(seconds);
    }
}
