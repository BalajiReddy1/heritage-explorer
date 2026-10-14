package com.example.heritage.data;

import com.example.heritage.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The 12 sites inserted into SQLite the first time the app runs.
 * Images live in res/drawable-nodpi and are looked up by key, because
 * R.drawable ids can change between builds and must not be stored in the database.
 */
public final class SiteSeed {

    public static final String MONUMENT = "Monument";
    public static final String FORT = "Fort";
    public static final String TEMPLE = "Temple";
    public static final String CAVE = "Cave";

    private static final String SUNRISE_SUNSET = "Sunrise to sunset";

    private static final Map<String, Integer> IMAGES = new HashMap<>();

    static {
        IMAGES.put("taj_mahal", R.drawable.taj_mahal);
        IMAGES.put("qutub_minar", R.drawable.qutub_minar);
        IMAGES.put("red_fort", R.drawable.red_fort);
        IMAGES.put("amber_fort", R.drawable.amber_fort);
        IMAGES.put("golconda_fort", R.drawable.golconda_fort);
        IMAGES.put("hampi", R.drawable.hampi);
        IMAGES.put("konark", R.drawable.konark);
        IMAGES.put("khajuraho", R.drawable.khajuraho);
        IMAGES.put("shore_temple", R.drawable.shore_temple);
        IMAGES.put("ajanta", R.drawable.ajanta);
        IMAGES.put("ellora", R.drawable.ellora);
        IMAGES.put("sanchi", R.drawable.sanchi);
    }

    private SiteSeed() {
    }

    public static int imageFor(String key) {
        Integer res = IMAGES.get(key);
        return res != null ? res : R.drawable.taj_mahal;
    }

    public static List<Site> all() {
        List<Site> list = new ArrayList<>();

        list.add(site("Taj Mahal", "Agra", "Uttar Pradesh", MONUMENT, "1632 to 1653",
                "An ivory white marble mausoleum on the south bank of the Yamuna, commissioned by the Mughal emperor Shah Jahan in memory of his wife Mumtaz Mahal. The complex brings together a formal garden, a mosque, a guest house and the main tomb in near perfect symmetry. It became a UNESCO World Heritage Site in 1983.",
                "30 minutes before sunrise to 30 minutes before sunset", Calendar.FRIDAY, 50, 1100,
                27.1751, 78.0421, "Taj_Mahal", "taj_mahal"));

        list.add(site("Qutub Minar", "New Delhi", "Delhi", MONUMENT, "1199 to 1220",
                "A 73 metre minaret of red sandstone and marble, started by Qutb ud-Din Aibak and completed by his successor Iltutmish. The complex around it includes the Quwwat-ul-Islam mosque and the Iron Pillar, which has resisted rust for more than 1,600 years.",
                SUNRISE_SUNSET, 0, 35, 550,
                28.5245, 77.1855, "Qutb_Minar", "qutub_minar"));

        list.add(site("Red Fort", "Delhi", "Delhi", FORT, "1639 to 1648",
                "The main residence of the Mughal emperors for nearly 200 years, built by Shah Jahan when he moved his capital from Agra to Delhi. Its red sandstone walls enclose palaces, audience halls and gardens. Every Independence Day the Prime Minister raises the national flag from its ramparts.",
                "9:30 AM to 4:30 PM", Calendar.MONDAY, 35, 500,
                28.6562, 77.2410, "Red_Fort", "red_fort"));

        list.add(site("Amber Fort", "Jaipur", "Rajasthan", FORT, "1592",
                "A hilltop fort of pale yellow and pink sandstone above Maota Lake, built by Raja Man Singh I. Inside, Rajput and Mughal styles meet in courtyards, gateways and the mirror lined Sheesh Mahal. It is one of the Hill Forts of Rajasthan listed by UNESCO in 2013.",
                "8:00 AM to 5:30 PM", 0, 100, 500,
                26.9855, 75.8513, "Amer_Fort", "amber_fort"));

        list.add(site("Golconda Fort", "Hyderabad", "Telangana", FORT, "16th century",
                "Once the capital of the Qutb Shahi dynasty and a famous diamond trading centre. The fort is known for its acoustics: a clap under the dome at the main gate can be heard at the hilltop pavilion, almost a kilometre away.",
                "8:00 AM to 5:30 PM", 0, 25, 300,
                17.3833, 78.4011, "Golconda", "golconda_fort"));

        list.add(site("Hampi", "Hampi", "Karnataka", TEMPLE, "14th to 16th century",
                "The ruins of Vijayanagara, one of the richest cities in the world in the 16th century, spread across a landscape of giant granite boulders. Highlights include the Virupaksha Temple, still in worship today, and the stone chariot at the Vittala Temple.",
                SUNRISE_SUNSET, 0, 40, 600,
                15.3350, 76.4600, "Hampi", "hampi"));

        list.add(site("Konark Sun Temple", "Konark", "Odisha", TEMPLE, "13th century",
                "Built by King Narasimhadeva I of the Eastern Ganga dynasty, the temple is designed as the giant chariot of the Sun god, with 24 carved stone wheels pulled by seven horses. The wheels work as sundials and show the time of day from the shadows of their spokes.",
                "6:00 AM to 8:00 PM", 0, 40, 600,
                19.8876, 86.0945, "Konark_Sun_Temple", "konark"));

        list.add(site("Khajuraho Temples", "Khajuraho", "Madhya Pradesh", TEMPLE, "950 to 1050",
                "A group of Hindu and Jain temples built by the Chandela dynasty, famous for finely detailed sculptures of gods, musicians, dancers and everyday life. Of about 85 original temples, around 20 survive. UNESCO World Heritage Site since 1986.",
                SUNRISE_SUNSET, 0, 40, 600,
                24.8318, 79.9199, "Khajuraho_Group_of_Monuments", "khajuraho"));

        list.add(site("Shore Temple", "Mahabalipuram", "Tamil Nadu", TEMPLE, "8th century",
                "One of the oldest structural stone temples in South India, built by the Pallava king Narasimhavarman II on the shore of the Bay of Bengal. It belongs to the Group of Monuments at Mahabalipuram, along with rock cut caves and the large open air relief known as Arjuna's Penance.",
                "6:00 AM to 6:00 PM", 0, 40, 600,
                12.6165, 80.1993, "Shore_Temple", "shore_temple"));

        list.add(site("Ajanta Caves", "Chhatrapati Sambhajinagar", "Maharashtra", CAVE, "2nd century BCE to 480 CE",
                "About 30 rock cut Buddhist caves carved into a horseshoe shaped cliff above the Waghora river. Their wall paintings are among the finest surviving examples of ancient Indian art, showing the life of the Buddha and the Jataka tales.",
                "9:00 AM to 5:30 PM", Calendar.MONDAY, 40, 600,
                20.5519, 75.7033, "Ajanta_Caves", "ajanta"));

        list.add(site("Ellora Caves", "Chhatrapati Sambhajinagar", "Maharashtra", CAVE, "600 to 1000 CE",
                "Thirty four caves carved side by side for the Buddhist, Hindu and Jain faiths. The centrepiece is the Kailasa Temple in Cave 16, cut from a single rock from the top down. It is one of the largest monolithic structures in the world.",
                "6:00 AM to 6:00 PM", Calendar.TUESDAY, 40, 600,
                20.0268, 75.1771, "Ellora_Caves", "ellora"));

        list.add(site("Sanchi Stupa", "Raisen", "Madhya Pradesh", MONUMENT, "3rd century BCE",
                "One of the oldest stone structures in India, commissioned by Emperor Ashoka. The great hemispherical stupa is surrounded by four carved gateways, called toranas, that tell stories from the life of the Buddha.",
                SUNRISE_SUNSET, 0, 40, 600,
                23.4793, 77.7398, "Sanchi", "sanchi"));

        return list;
    }

    private static Site site(String name, String city, String state, String category, String built,
                             String about, String timings, int closedDay, int feeIndian, int feeForeign,
                             double lat, double lng, String wikiPage, String imageKey) {
        Site s = new Site();
        s.name = name;
        s.city = city;
        s.state = state;
        s.category = category;
        s.built = built;
        s.about = about;
        s.timings = timings;
        s.closedDay = closedDay;
        s.feeIndian = feeIndian;
        s.feeForeign = feeForeign;
        s.lat = lat;
        s.lng = lng;
        s.wikiUrl = "https://en.wikipedia.org/wiki/" + wikiPage;
        s.imageKey = imageKey;
        return s;
    }
}
