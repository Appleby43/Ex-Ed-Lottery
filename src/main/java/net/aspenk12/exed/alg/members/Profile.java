package net.aspenk12.exed.alg.members;

import net.aspenk12.exed.alg.containers.Gender;
import net.aspenk12.exed.alg.containers.Grade;
import net.aspenk12.exed.ui.ErrorAlert;
import net.aspenk12.exed.util.*;

import java.util.*;

/**
 * A (Student) Profile contains 'validated' data about a student. It comprises of data specified in the validated CSV.
 */
public class Profile {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected Gender gender;
    protected Grade grade;
    protected int points;
    protected int lottoNumber;

    private final List<Course> previousCourses;

    private static Map<Integer, Profile> profiles;

    //on validation, track the used lotto numbers to make sure nobody has the same lotto number.
    private static Set<Integer> usedLottoNumbers = new HashSet<>();

    /**
     * This constructor is public for testing, but in practice it should only be called from inside the class.
     */
    public Profile(int id, String firstName, String lastName, Gender gender, Grade grade, int points, int lottoNumber, List<Course> previousCourses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.grade = grade;
        this.points = points;
        this.lottoNumber = lottoNumber;
        this.previousCourses = previousCourses;

        //validate lotto numbers
        if(usedLottoNumbers.contains(lottoNumber)){
            Warnings.logWarning("Profile with ID " + id + " shares a lottery number with another student.");
        } else {
            usedLottoNumbers.add(lottoNumber);
        }
    }

    public static void createProfiles(CSV csv) {
        if(profiles != null) return;

        profiles = new HashMap<>();

        for (int i = 0; i < csv.rows(); i++) {
            String[] row = csv.get(i);

            String email, firstName, lastName, genderString, gradeString, pointString, lottoString;
            try{
                email = row[0];
                firstName = row[1];
                lastName = row[2];
                genderString = row[3];
                gradeString = row[4];
                pointString = row[5];
                lottoString = row[6];
            } catch (ArrayIndexOutOfBoundsException e){
                //todo maybe convert this to a BadDataException?
                ErrorAlert.throwErrorWindow("Validated Student Data is missing.",
                        "Check to make sure the validated student data is complete and correctly formatted." +
                                " More info about this problem can be found in the printed stack trace.", true);
                e.printStackTrace();
                return;
            }

            int id = Util.getIDFromEmail(email);

            //todo try to lambdaize all of these try-catches
            Gender gender;
            try {
                gender = Gender.getFromString(genderString);
            } catch (Exception e) {
                throw new BadDataException(csv, i, 3, genderString);
            }

            Grade grade;
            try {
                int gradeInt = Integer.parseInt(gradeString);
                grade = Grade.getFromInt(gradeInt);
            } catch (RuntimeException e){
                throw new BadDataException(csv, i, 4, gradeString);
            }

            int points = Util.parseIntCSV(pointString, csv, i, 5 );

            int lottoNumber = Util.parseIntCSV(lottoString, csv, i, 6);

            List<Course> previousCourses = new ArrayList<>();

            //loop through previous course cells
            for (int j = 7; j < 10; j++) {

                String courseID;

                try{
                    courseID = row[j];
                } catch (ArrayIndexOutOfBoundsException e){
                    //if no previous course data exists, just skip out of this loop entirely.
                    break;
                }

                //obviously don't try to add empty cells to previous courses
                if(!courseID.equals("")){
                    Course c = Course.get(courseID);
                    if(c == null) throw new BadDataException(csv, i, j, courseID);

                    previousCourses.add(c);
                }
            }

            Profile profile = new Profile(id, firstName, lastName, gender, grade, points, lottoNumber, previousCourses);
            profiles.put(id, profile);
        }
    }

    public static Profile get(int id) {
        return profiles.get(id);
    }

    public static int count(){
        return profiles.size();
    }

    public List<Course> getPreviousCourses(){
        return previousCourses;
    }

    public String getFullName(){
        return getFirstName() + " " + getLastName();
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public Grade getGrade() {
        return grade;
    }

    public int getPoints() {
        return points;
    }

    public int getLottoNumber() {
        return lottoNumber;
    }
}
