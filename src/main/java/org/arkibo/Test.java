package org.arkibo;

import org.arkibo.repository.ThesisRepository;
import org.arkibo.dto.Response;
import org.arkibo.dto.thesis.ThesisCreateRequest;
import org.arkibo.dto.thesis.AuthorCreateRequest;
import org.arkibo.dto.thesis.KeywordCreateRequest;
import org.arkibo.models.ThesisModels.College;
import org.arkibo.models.ThesisModels.Thesis;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        ThesisRepository tr = new ThesisRepository();

        Object[][] mockData = {
            {
                "Machine Learning Approach for Early Detection of Rice Diseases",
                List.of(new AuthorCreateRequest("Paolo Villanueva"), new AuthorCreateRequest("Christine Tan")),
                List.of(new KeywordCreateRequest("machine learning"), new KeywordCreateRequest("rice diseases"), new KeywordCreateRequest("image classification")),
                "This study develops a convolutional neural network model for detecting rice leaf diseases from images. The model achieved 94% accuracy in classifying bacterial blight, blast, and tungro.",
                2024, "capstone", College.CEN
            },
            {
                "Job Satisfaction Among Public School Teachers in Quezon City",
                List.of(new AuthorCreateRequest("Lorna Magbanua")),
                List.of(new KeywordCreateRequest("job satisfaction"), new KeywordCreateRequest("teachers"), new KeywordCreateRequest("public schools")),
                "This descriptive-correlational study examines factors affecting job satisfaction among public school teachers. Compensation and administrative support were identified as key determinants.",
                2023, "quantitative", College.CED
            },
            {
                "Consumer Behavior Towards Sustainable Fashion",
                List.of(new AuthorCreateRequest("Michelle Uy"), new AuthorCreateRequest("David Lim")),
                List.of(new KeywordCreateRequest("sustainable fashion"), new KeywordCreateRequest("consumer behavior"), new KeywordCreateRequest("environmental awareness")),
                "This research explores consumer attitudes and purchasing behavior towards sustainable fashion brands. Results show that 45% of respondents are willing to pay premium prices for eco-friendly clothing.",
                2024, "quantitative", College.CBA
            },
            {
                "Development of an Automated Attendance System Using Facial Recognition",
                List.of(new AuthorCreateRequest("Jerome Castillo"), new AuthorCreateRequest("Bianca Flores"), new AuthorCreateRequest("Ryan Gonzales")),
                List.of(new KeywordCreateRequest("facial recognition"), new KeywordCreateRequest("attendance system"), new KeywordCreateRequest("automation")),
                "This capstone project implements an automated attendance system using facial recognition technology. The system processes attendance in real-time with 98% recognition accuracy.",
                2023, "capstone", College.CEN
            },
            {
                "Water Quality Assessment of Laguna de Bay Tributaries",
                List.of(new AuthorCreateRequest("Nicole Perez")),
                List.of(new KeywordCreateRequest("water quality"), new KeywordCreateRequest("Laguna de Bay"), new KeywordCreateRequest("environmental science")),
                "This study assesses the physicochemical parameters of five tributaries flowing into Laguna de Bay. Elevated levels of phosphates and nitrates indicate agricultural runoff contamination.",
                2022, "quantitative", College.COS
            },
            {
                "Challenges Faced by Student-Athletes in Balancing Academics and Sports",
                List.of(new AuthorCreateRequest("Marco Antonio"), new AuthorCreateRequest("Jasmine Reyes")),
                List.of(new KeywordCreateRequest("student-athletes"), new KeywordCreateRequest("time management"), new KeywordCreateRequest("academic-athletic balance")),
                "This phenomenological study explores the challenges faced by varsity student-athletes in managing their dual responsibilities. Time constraints and physical fatigue emerged as primary concerns.",
                2024, "qualitative", College.CASS
            },
            {
                "Soil Fertility Analysis in Urban Community Gardens",
                List.of(new AuthorCreateRequest("Fernando Cruz")),
                List.of(new KeywordCreateRequest("soil fertility"), new KeywordCreateRequest("urban gardens"), new KeywordCreateRequest("community agriculture")),
                "This research analyzes soil nutrient content in urban community gardens across Metro Manila. Results indicate nitrogen deficiency in 70% of sampled plots requiring organic amendments.",
                2023, "quantitative", College.CAG
            },
            {
                "Mental Health Awareness Among Senior High School Students",
                List.of(new AuthorCreateRequest("Andrea Lim"), new AuthorCreateRequest("Carlos Mendoza")),
                List.of(new KeywordCreateRequest("mental health"), new KeywordCreateRequest("awareness"), new KeywordCreateRequest("senior high school")),
                "This study measures the level of mental health awareness among senior high school students. Findings reveal misconceptions about depression and anxiety requiring educational intervention.",
                2024, "quantitative", College.CHSI
            },
            {
                "Antimicrobial Properties of Propolis from Native Stingless Bees",
                List.of(new AuthorCreateRequest("Elena Ramos"), new AuthorCreateRequest("Jonathan Cruz")),
                List.of(new KeywordCreateRequest("propolis"), new KeywordCreateRequest("antimicrobial"), new KeywordCreateRequest("stingless bees")),
                "This experimental study evaluates the antimicrobial properties of propolis extracted from native stingless bees. The extract showed significant inhibitory effects against S. aureus and E. coli.",
                2023, "quantitative", College.CVSM
            },
            {
                "E-Commerce Adoption Among MSMEs During the Pandemic",
                List.of(new AuthorCreateRequest("Benedict Sy"), new AuthorCreateRequest("Rachel Tan"), new AuthorCreateRequest("Andrew Go")),
                List.of(new KeywordCreateRequest("e-commerce"), new KeywordCreateRequest("MSME"), new KeywordCreateRequest("digital transformation")),
                "This study examines factors influencing e-commerce adoption among micro, small, and medium enterprises during COVID-19. Digital literacy and infrastructure emerged as significant barriers.",
                2022, "quantitative", College.CBA
            },
            {
                "Development of a Library Management System with Book Recommendation",
                List.of(new AuthorCreateRequest("Kenneth Ong"), new AuthorCreateRequest("Mary Grace Santos")),
                List.of(new KeywordCreateRequest("library system"), new KeywordCreateRequest("recommendation engine"), new KeywordCreateRequest("web application")),
                "This capstone project develops a web-based library management system featuring a collaborative filtering book recommendation engine. User testing showed 89% satisfaction with recommendations.",
                2024, "capstone", College.CEN
            },
            {
                "Reading Comprehension Strategies of ESL Learners",
                List.of(new AuthorCreateRequest("Victoria Hernandez")),
                List.of(new KeywordCreateRequest("reading comprehension"), new KeywordCreateRequest("ESL"), new KeywordCreateRequest("learning strategies")),
                "This qualitative study investigates reading comprehension strategies employed by English as Second Language learners. Scanning and contextual guessing were the most frequently used strategies.",
                2023, "qualitative", College.CED
            },
            {
                "Prevalence of Musculoskeletal Disorders Among Office Workers",
                List.of(new AuthorCreateRequest("Albert Tan"), new AuthorCreateRequest("Jennifer Lopez")),
                List.of(new KeywordCreateRequest("musculoskeletal disorders"), new KeywordCreateRequest("office workers"), new KeywordCreateRequest("occupational health")),
                "This cross-sectional study determines the prevalence of musculoskeletal disorders among office workers in Makati. Lower back pain affected 65% of respondents with prolonged sitting as risk factor.",
                2024, "quantitative", College.CHSI
            },
            {
                "Indigenous Knowledge on Traditional Fishing Practices in Palawan",
                List.of(new AuthorCreateRequest("Ramon Aquino")),
                List.of(new KeywordCreateRequest("indigenous knowledge"), new KeywordCreateRequest("fishing practices"), new KeywordCreateRequest("Palawan")),
                "This ethnographic study documents traditional fishing practices and indigenous knowledge of coastal communities in Palawan. Sustainable practices passed through generations are at risk of being lost.",
                2022, "qualitative", College.CASS
            },
            {
                "Cybersecurity Awareness Among Online Banking Users",
                List.of(new AuthorCreateRequest("Dennis Garcia"), new AuthorCreateRequest("Kristine Reyes"), new AuthorCreateRequest("Paolo Santos")),
                List.of(new KeywordCreateRequest("cybersecurity"), new KeywordCreateRequest("online banking"), new KeywordCreateRequest("digital literacy")),
                "This study assesses cybersecurity awareness levels among online banking users in Metro Manila. Only 35% of respondents could identify common phishing techniques indicating need for education.",
                2024, "quantitative", College.CEN
            }
        };

        for (Object[] data : mockData) {
            String title = (String) data[0];
            @SuppressWarnings("unchecked")
            List<AuthorCreateRequest> authors = (List<AuthorCreateRequest>) data[1];
            @SuppressWarnings("unchecked")
            List<KeywordCreateRequest> keywords = (List<KeywordCreateRequest>) data[2];
            String abstractText = (String) data[3];
            int year = (int) data[4];
            String researchType = (String) data[5];
            College college = (College) data[6];

            ThesisCreateRequest req = new ThesisCreateRequest(
                    title,
                    authors,
                    abstractText,
                    keywords,
                    year,
                    researchType,
                    college);

            try {
                Response<Thesis> res = tr.addThesis(req);
                if (res.ok()) {
                    Thesis created = res.data();
                    System.out.println("Inserted thesis: id=" + created.id() + " title='" + created.title() + "'");
                } else {
                    System.out.println("Failed to insert thesis: " + res.message());
                }
            } catch (Exception e) {
                System.out.println("Exception while inserting thesis: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
