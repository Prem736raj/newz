package com.example.data.local

import com.example.data.model.CaseDossier
import com.example.data.model.CaseMediaAttachment
import com.example.data.model.CaseStage
import com.example.data.model.CityJurisdiction
import com.example.data.model.DistrictJurisdiction
import com.example.data.model.EmergencyHelpline
import com.example.data.model.LegalRightGuide
import com.example.data.model.MediaAttachmentType
import com.example.data.model.SensitivityLevel
import com.example.data.model.StateJurisdiction
import com.example.data.model.TimelineUpdateItem

object SurakshaArchiveSeedData {

    private const val DAY_MS = 86_400_000L
    private val nowMs: Long
        get() = System.currentTimeMillis()

    val stateJurisdictions: List<StateJurisdiction> = listOf(
        StateJurisdiction(
            stateName = "Andhra Pradesh",
            stateCode = "AP",
            annualReportedCrimesAgainstWomen = 25503,
            chargeSheetRatePercent = 82.4,
            pocsoMinorCasesAnnual = 2890,
            keySafetyConcern = "School & college campus security breaches, stalking of minor students, and cyber blackmail.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Anakapalli",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Elamanchili",
                            mlaName = "Sundarapu Vijay Kumar",
                            mlaConstituency = "Elamanchili AC",
                            mlaParty = "JSP",
                            mpName = "C. M. Ramesh",
                            mpConstituency = "Anakapalli Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Anakapalli District Police • Disha PS",
                            activeTrackedCases = 4
                        ),
                        CityJurisdiction(
                            cityName = "Anakapalli Town",
                            mlaName = "Konathala Ramakrishna",
                            mlaConstituency = "Anakapalli AC",
                            mlaParty = "JSP",
                            mpName = "C. M. Ramesh",
                            mpConstituency = "Anakapalli Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Anakapalli Town PS",
                            activeTrackedCases = 3
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Visakhapatnam",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Visakhapatnam City",
                            mlaName = "Ganta Srinivasa Rao",
                            mlaConstituency = "Bhimili / Visakhapatnam North",
                            mlaParty = "TDP",
                            mpName = "Mathukumilli Bharat",
                            mpConstituency = "Visakhapatnam Lok Sabha",
                            mpParty = "TDP",
                            policeRange = "Visakhapatnam City Police Commissionerate",
                            activeTrackedCases = 7
                        ),
                        CityJurisdiction(
                            cityName = "Gajuwaka",
                            mlaName = "Palla Srinivasa Rao",
                            mlaConstituency = "Gajuwaka AC",
                            mlaParty = "TDP",
                            mpName = "Mathukumilli Bharat",
                            mpConstituency = "Visakhapatnam Lok Sabha",
                            mpParty = "TDP",
                            policeRange = "Gajuwaka Zone PS",
                            activeTrackedCases = 5
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Guntur",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Guntur City",
                            mlaName = "Galla Madhavi",
                            mlaConstituency = "Guntur West AC",
                            mlaParty = "TDP",
                            mpName = "Chandra Sekhar Pemmasani",
                            mpConstituency = "Guntur Lok Sabha",
                            mpParty = "TDP",
                            policeRange = "Guntur Urban District Police",
                            activeTrackedCases = 6
                        ),
                        CityJurisdiction(
                            cityName = "Tenali",
                            mlaName = "Nadendla Manohar",
                            mlaConstituency = "Tenali AC",
                            mlaParty = "JSP",
                            mpName = "Chandra Sekhar Pemmasani",
                            mpConstituency = "Guntur Lok Sabha",
                            mpParty = "TDP",
                            policeRange = "Tenali Sub-Division PS",
                            activeTrackedCases = 2
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Tirupati",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Tirupati Urban",
                            mlaName = "Arani Srinivasulu",
                            mlaConstituency = "Tirupati AC",
                            mlaParty = "JSP",
                            mpName = "Maddila Gurumoorthy",
                            mpConstituency = "Tirupati Lok Sabha",
                            mpParty = "YSRCP",
                            policeRange = "Tirupati Urban Police District",
                            activeTrackedCases = 4
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Uttar Pradesh",
            stateCode = "UP",
            annualReportedCrimesAgainstWomen = 65743,
            chargeSheetRatePercent = 75.8,
            pocsoMinorCasesAnnual = 8120,
            keySafetyConcern = "Crimes against teenage girls in rural & semi-urban belts, kidnapping, and witness intimidation.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Lucknow",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Lucknow Central",
                            mlaName = "Ravidas Mehrotra",
                            mlaConstituency = "Lucknow Central AC",
                            mlaParty = "SP",
                            mpName = "Rajnath Singh",
                            mpConstituency = "Lucknow Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Lucknow Police Commissionerate • Women Power Line 1090",
                            activeTrackedCases = 9
                        ),
                        CityJurisdiction(
                            cityName = "Gomti Nagar",
                            mlaName = "O. P. Srivastava",
                            mlaConstituency = "Lucknow East AC",
                            mlaParty = "BJP",
                            mpName = "Rajnath Singh",
                            mpConstituency = "Lucknow Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Lucknow East Zone PS",
                            activeTrackedCases = 4
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Varanasi",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Lanka / BHU Campus Belt",
                            mlaName = "Saurabh Srivastava",
                            mlaConstituency = "Varanasi Cantt AC",
                            mlaParty = "BJP",
                            mpName = "Narendra Modi",
                            mpConstituency = "Varanasi Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Varanasi Commissionerate • Lanka PS",
                            activeTrackedCases = 6
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Gautam Buddha Nagar",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Noida",
                            mlaName = "Pankaj Singh",
                            mlaConstituency = "Noida AC",
                            mlaParty = "BJP",
                            mpName = "Mahesh Sharma",
                            mpConstituency = "Gautam Buddha Nagar Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Noida Commissionerate • Women Safety Cell",
                            activeTrackedCases = 5
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Maharashtra",
            stateCode = "MH",
            annualReportedCrimesAgainstWomen = 45331,
            chargeSheetRatePercent = 80.2,
            pocsoMinorCasesAnnual = 7450,
            keySafetyConcern = "School minor safety lapses, commuter & workplace harassment, and cyber morphing.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Thane",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Badlapur",
                            mlaName = "Kisan Shankar Kathore",
                            mlaConstituency = "Murbad / Badlapur AC",
                            mlaParty = "BJP",
                            mpName = "Suresh Gopinath Mhatre",
                            mpConstituency = "Bhiwandi Lok Sabha",
                            mpParty = "NCP-SP",
                            policeRange = "Thane City Police • Badlapur East PS",
                            activeTrackedCases = 8
                        ),
                        CityJurisdiction(
                            cityName = "Thane City",
                            mlaName = "Sanjay Kelkar",
                            mlaConstituency = "Thane AC",
                            mlaParty = "BJP",
                            mpName = "Naresh Mhaske",
                            mpConstituency = "Thane Lok Sabha",
                            mpParty = "SHS",
                            policeRange = "Thane Police Commissionerate",
                            activeTrackedCases = 6
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Pune",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Swargate / Pune Central",
                            mlaName = "Madhuri Misal",
                            mlaConstituency = "Parvati AC",
                            mlaParty = "BJP",
                            mpName = "Murlidhar Mohol",
                            mpConstituency = "Pune Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Pune City Police • Damini Marshals",
                            activeTrackedCases = 7
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Mumbai Suburban",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Andheri",
                            mlaName = "Ameet Satam",
                            mlaConstituency = "Andheri West AC",
                            mlaParty = "BJP",
                            mpName = "Ravindra Waikar",
                            mpConstituency = "Mumbai North West Lok Sabha",
                            mpParty = "SHS",
                            policeRange = "Mumbai Police • Nirbhaya Squad",
                            activeTrackedCases = 5
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "West Bengal",
            stateCode = "WB",
            annualReportedCrimesAgainstWomen = 34738,
            chargeSheetRatePercent = 88.6,
            pocsoMinorCasesAnnual = 4310,
            keySafetyConcern = "Night-shift workplace security in hospitals & colleges, trafficking, and delayed FIR registration.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Kolkata",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Shyambazar / North Kolkata",
                            mlaName = "Dr. Shashi Panja",
                            mlaConstituency = "Shyampukur AC",
                            mlaParty = "AITC",
                            mpName = "Sudip Bandyopadhyay",
                            mpConstituency = "Kolkata Uttar Lok Sabha",
                            mpParty = "AITC",
                            policeRange = "Kolkata Police • Tala PS / Lalbazar Detective Dept",
                            activeTrackedCases = 9
                        ),
                        CityJurisdiction(
                            cityName = "Jadavpur / South Kolkata",
                            mlaName = "Debabrata Majumdar",
                            mlaConstituency = "Jadavpur AC",
                            mlaParty = "AITC",
                            mpName = "Saayoni Ghosh",
                            mpConstituency = "Jadavpur Lok Sabha",
                            mpParty = "AITC",
                            policeRange = "Kolkata Police South Suburban Division",
                            activeTrackedCases = 5
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "North 24 Parganas",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Barasat",
                            mlaName = "Chiranjeet Chakraborty",
                            mlaConstituency = "Barasat AC",
                            mlaParty = "AITC",
                            mpName = "Dr. Kakoli Ghosh Dastidar",
                            mpConstituency = "Barasat Lok Sabha",
                            mpParty = "AITC",
                            policeRange = "Barasat Police District",
                            activeTrackedCases = 6
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Delhi NCR",
            stateCode = "DL",
            annualReportedCrimesAgainstWomen = 14247,
            chargeSheetRatePercent = 79.1,
            pocsoMinorCasesAnnual = 3190,
            keySafetyConcern = "Stalking, acid/weapon threats against teenage girls, and dark-spot transit corridors.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "South West Delhi",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Dwarka / Uttam Nagar",
                            mlaName = "Parduymn Singh Rajput",
                            mlaConstituency = "Dwarka AC",
                            mlaParty = "BJP",
                            mpName = "Kamaljeet Sehrawat",
                            mpConstituency = "West Delhi Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Delhi Police • Dwarka District SPUWAC",
                            activeTrackedCases = 7
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "North Delhi",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Shahbad Dairy / Rohini",
                            mlaName = "Vijender Gupta",
                            mlaConstituency = "Rohini AC",
                            mlaParty = "BJP",
                            mpName = "Yogendra Chandolia",
                            mpConstituency = "North West Delhi Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Delhi Police • Outer North District",
                            activeTrackedCases = 8
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Rajasthan",
            stateCode = "RJ",
            annualReportedCrimesAgainstWomen = 45058,
            chargeSheetRatePercent = 69.5,
            pocsoMinorCasesAnnual = 5140,
            keySafetyConcern = "Coaching hub student safety, minor abduction, and rural panchayat suppression of complaints.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Kota",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Kota Coaching Belt",
                            mlaName = "Sandeep Sharma",
                            mlaConstituency = "Kota South AC",
                            mlaParty = "BJP",
                            mpName = "Om Birla",
                            mpConstituency = "Kota Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Kota City Police • Student Safety Cell",
                            activeTrackedCases = 6
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Jaipur",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Jaipur Civil Lines",
                            mlaName = "Gopal Sharma",
                            mlaConstituency = "Civil Lines AC",
                            mlaParty = "BJP",
                            mpName = "Manju Sharma",
                            mpConstituency = "Jaipur Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Jaipur Police Commissionerate • निर्भया Squad",
                            activeTrackedCases = 7
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Bihar",
            stateCode = "BR",
            annualReportedCrimesAgainstWomen = 20222,
            chargeSheetRatePercent = 76.2,
            pocsoMinorCasesAnnual = 2780,
            keySafetyConcern = "Attacks on schoolgirls commuting between villages, cyber extortion, and dowry violence.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Patna",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Patna Sahib / Kankarbagh",
                            mlaName = "Nitin Nabin",
                            mlaConstituency = "Bankipur AC",
                            mlaParty = "BJP",
                            mpName = "Ravi Shankar Prasad",
                            mpConstituency = "Patna Sahib Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Patna Central Range • Mahila Thana",
                            activeTrackedCases = 6
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Muzaffarpur",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Muzaffarpur Town",
                            mlaName = "Vijendra Chaudhary",
                            mlaConstituency = "Muzaffarpur AC",
                            mlaParty = "INC",
                            mpName = "Raj Bhushan Choudhary",
                            mpConstituency = "Muzaffarpur Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Tirhut Range • Muzaffarpur Mahila PS",
                            activeTrackedCases = 5
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Karnataka",
            stateCode = "KA",
            annualReportedCrimesAgainstWomen = 16957,
            chargeSheetRatePercent = 83.1,
            pocsoMinorCasesAnnual = 3420,
            keySafetyConcern = "College campus stalking, hostel security breaches, and tech-corridor night transit safety.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Hubballi-Dharwad",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Hubballi Campus Zone",
                            mlaName = "Mahesh Tenginakai",
                            mlaConstituency = "Hubli-Dharwad Central AC",
                            mlaParty = "BJP",
                            mpName = "Pralhad Joshi",
                            mpConstituency = "Dharwad Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Hubballi-Dharwad Police Commissionerate • Chennamma Squad",
                            activeTrackedCases = 5
                        )
                    )
                ),
                DistrictJurisdiction(
                    districtName = "Bengaluru Urban",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Bengaluru Central / Koramangala",
                            mlaName = "Ramalinga Reddy",
                            mlaConstituency = "BTM Layout AC",
                            mlaParty = "INC",
                            mpName = "P. C. Mohan",
                            mpConstituency = "Bangalore Central Lok Sabha",
                            mpParty = "BJP",
                            policeRange = "Bengaluru City Police • Parihar Women Cell",
                            activeTrackedCases = 8
                        )
                    )
                )
            )
        ),
        StateJurisdiction(
            stateName = "Tamil Nadu",
            stateCode = "TN",
            annualReportedCrimesAgainstWomen = 9207,
            chargeSheetRatePercent = 81.9,
            pocsoMinorCasesAnnual = 4580,
            keySafetyConcern = "University campus security, POCSO cases in schools, and digital harassment.",
            districts = listOf(
                DistrictJurisdiction(
                    districtName = "Chennai",
                    cities = listOf(
                        CityJurisdiction(
                            cityName = "Guindy / University Zone",
                            mlaName = "J. M. H. Aassan Maulaana",
                            mlaConstituency = "Velachery / Guindy AC",
                            mlaParty = "INC",
                            mpName = "Thamizhachi Thangapandian",
                            mpConstituency = "Chennai South Lok Sabha",
                            mpParty = "DMK",
                            policeRange = "Greater Chennai Police • All Women Police Station (AWPS)",
                            activeTrackedCases = 7
                        )
                    )
                )
            )
        )
    )

    fun buildInitialCaseDossiers(): List<CaseDossier> = listOf(
        CaseDossier(
            id = "case_ap_school_chainsaw_01",
            title = "Andhra Pradesh School Security Breach: 21-Year-Old Attacks Class 10 Girl with Mechanical Chainsaw Inside Classroom",
            summary = "A highly disturbing security breach has shocked Andhra Pradesh, where a 21-year-old man walked past school gates carrying a mechanical chainsaw and attacked a 15-year-old Class 10 student inside her classroom after she rejected his stalking advances.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                A deeply disturbing security breach shocked the state of Andhra Pradesh when a 21-year-old man entered a high school campus and attacked a Class 10 teenage girl with a mechanical chainsaw right inside her classroom in front of terrified students and staff.
                
                1. HOW THE SECURITY BREACH HAPPENED:
                According to police investigation and eyewitness statements from students, the 21-year-old accused had been stalking and harassing the Class 10 student for several weeks. Despite prior warnings, the intruder managed to breach the school perimeter carrying a heavy mechanical chainsaw concealed in a bag, walking directly into the academic block during school hours without being stopped by gate security.
                
                2. THE CLASSROOM ATTACK & STUDENT INTERVENTION:
                Entering the Class 10 classroom, the attacker revved the mechanical chainsaw and lunged at the teenage girl, inflicting severe injuries to her neck, shoulder, and hand as she tried to shield herself. Hearing the screams and the roar of the chainsaw, teachers and senior staff rushed in, overpowered the attacker, seized the weapon, and rushed the bleeding student to the emergency trauma center.
                
                3. WHY THIS CASE MUST NEVER BE FORGOTTEN:
                Public outrage often peaks for 48 hours and fades after one or two months, leaving survivors and families fighting prolonged legal battles alone while school security reforms remain on paper. This dossier tracks every monthly development: the survivor's reconstructive recovery, the POCSO and BNS Section 109 (Attempt to Murder) fast-track prosecution, mandatory perimeter security audits across Andhra Pradesh schools, and direct accountability of the constituency's MLA and MP.
            """.trimIndent(),
            stateName = "Andhra Pradesh",
            districtName = "Anakapalli",
            cityName = "Elamanchili",
            mlaName = "Sundarapu Vijay Kumar",
            mlaConstituency = "Elamanchili AC",
            mlaParty = "JSP",
            mpName = "C. M. Ramesh",
            mpConstituency = "Anakapalli Lok Sabha",
            mpParty = "BJP",
            policeJurisdiction = "Anakapalli District Police • Disha Women PS",
            victimCategory = "Teenage Student (13-19)",
            crimeCategory = "School/Campus Attack",
            caseStage = CaseStage.CHARGESHEET_FILED,
            incidentDateLabel = "76 Days Ago • Tracked Monthly",
            incidentEpochMillis = nowMs - (76L * DAY_MS),
            sensitivityLevel = SensitivityLevel.HIGH_DISTURBANCE,
            triggerWarning = "Trigger Warning: Graphic weapon attack (mechanical chainsaw) on a Class 10 minor schoolgirl inside a classroom.",
            relevantLaws = listOf(
                "BNS Section 109 (Attempt to Murder)",
                "POCSO Act Sections 11 & 12 (Stalking & Assault of Minor)",
                "BNS Section 78 (Stalking)",
                "AP Disha Special Court Fast-Track Mandate"
            ),
            sourceName = "Suraksha Permanent Archive • Andhra Pradesh Bureau",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Andhra_Pradesh_High_Court.jpg/960px-Andhra_Pradesh_High_Court.jpg",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            mediaCaption = "Anakapalli District Police SP Press Briefing, Campus Perimeter Security Audit & Special POCSO Court Record (Minor survivor identity protected under POCSO Act Sec. 23)",
            mediaAttachments = listOf(
                CaseMediaAttachment(
                    id = "media_ap_photo_1",
                    type = MediaAttachmentType.IMAGE,
                    url = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Andhra_Pradesh_High_Court.jpg/960px-Andhra_Pradesh_High_Court.jpg",
                    title = "Andhra Pradesh Judicial & Special POCSO Court Complex",
                    caption = "Forensic chargesheet and 18 student eyewitness statements filed before the Special Court in Andhra Pradesh following the Elamanchili classroom attack.",
                    sourceCredit = "Judicial Press Archive / Public Domain"
                ),
                CaseMediaAttachment(
                    id = "media_ap_photo_2",
                    type = MediaAttachmentType.IMAGE,
                    url = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Visakhapatnam_Police_Vehicle.jpg/960px-Visakhapatnam_Police_Vehicle.jpg",
                    title = "Anakapalli / Visakhapatnam Range Police Security Inspection",
                    caption = "District Police and Disha Women Protection Wing security perimeter audit outside the school gate after the security breach.",
                    sourceCredit = "AP Police Press Release"
                ),
                CaseMediaAttachment(
                    id = "media_ap_video_1",
                    type = MediaAttachmentType.VIDEO,
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Andhra_Pradesh_High_Court.jpg/960px-Andhra_Pradesh_High_Court.jpg",
                    title = "Incident Report & District Police Press Briefing Video",
                    caption = "Verified incident report covering the classroom security breach, immediate arrest of the 21-year-old accused, weapon seizure, and DEO school gate inquiry.",
                    sourceCredit = "AP Bureau Broadcast & Police Briefing",
                    durationLabel = "0:15 • Press Clip",
                    externalBroadcastUrl = "https://www.youtube.com/results?search_query=Andhra+Pradesh+Anakapalli+school+girl+attack+police+briefing+news"
                )
            ),
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_ap_1",
                    caseId = "case_ap_school_chainsaw_01",
                    dateLabel = "Day 1 • Incident & Immediate Arrest",
                    stageBadge = "FIR_FILED",
                    headline = "Attacker overpowered inside Class 10 classroom; survivor air-rushed to trauma ICU",
                    details = "Police seized the mechanical chainsaw on campus, registered an FIR under BNS Section 109 (Attempt to Murder) and the POCSO Act, and remanded the 21-year-old accused to judicial custody.",
                    sourceName = "District Police Bulletin"
                ),
                TimelineUpdateItem(
                    id = "upd_ap_2",
                    caseId = "case_ap_school_chainsaw_01",
                    dateLabel = "Month 1 Follow-Up • Day 32",
                    stageBadge = "ACCUSED_ARRESTED",
                    headline = "Bail plea rejected; DEO inquiry exposes absence of trained security guards at school gate",
                    details = "One month after headlines faded from national TV, the District Educational Officer submitted an inquiry report confirming the school lacked functional CCTV and gate frisking. The survivor underwent second reconstructive surgery.",
                    sourceName = "Suraksha 30-Day Follow-Up Desk"
                ),
                TimelineUpdateItem(
                    id = "upd_ap_3",
                    caseId = "case_ap_school_chainsaw_01",
                    dateLabel = "Month 2 Follow-Up • Day 64",
                    stageBadge = "CHARGESHEET_FILED",
                    headline = "Comprehensive forensic chargesheet filed; parents demand permanent school safety marshals from MLA & MP",
                    details = "Forensic analysis of the chainsaw, digital stalking records, and 18 student eyewitness statements were filed before the Special POCSO Court. Parents' association submitted a memorandum to local MLA Sundarapu Vijay Kumar and MP C. M. Ramesh demanding a constituency-wide school safety audit.",
                    sourceName = "Suraksha 60-Day Follow-Up Desk"
                )
            )
        ),
        CaseDossier(
            id = "case_mh_badlapur_school_02",
            title = "Badlapur School Minor Assault Case: Two 4-Year-Old Schoolgirls Assaulted Inside School Washroom; Institutional Cover-Up Tracked",
            summary = "Two minor girls were sexually assaulted by a school attendant inside their school premises in Badlapur, Thane district, triggering massive citizen protests after police initially delayed registering the FIR for over 11 hours.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                Two four-year-old girls studying in kindergarten at a prominent school in Badlapur (Thane district, Maharashtra) were sexually assaulted by a male cleaning attendant hired without mandatory police verification.
                
                1. INSTITUTIONAL FAILURE & 11-HOUR FIR DELAY:
                When the traumatized parents approached the school management and local police station, the school allegedly attempted to suppress the incident to protect its reputation, and the local police officer made the parents wait over 11 hours before recording a Zero/POCSO FIR.
                
                2. LONG-TERM ACCOUNTABILITY & WHY WE TRACK THIS:
                Months after the rail-roko protests subsided, questions remain regarding the prosecution of school trustees for POCSO Section 21 (failure to report) and compliance with the Bombay High Court's mandatory school safety committee directives across Maharashtra districts.
            """.trimIndent(),
            stateName = "Maharashtra",
            districtName = "Thane",
            cityName = "Badlapur",
            mlaName = "Kisan Shankar Kathore",
            mlaConstituency = "Murbad / Badlapur AC",
            mlaParty = "BJP",
            mpName = "Suresh Gopinath Mhatre",
            mpConstituency = "Bhiwandi Lok Sabha",
            mpParty = "NCP-SP",
            policeJurisdiction = "Thane City Police • Badlapur East PS / SIT",
            victimCategory = "Schoolgirl / Minor (<12)",
            crimeCategory = "POCSO / Minor Assault",
            caseStage = CaseStage.TRIAL_ONGOING,
            incidentDateLabel = "112 Days Ago • Tracked Monthly",
            incidentEpochMillis = nowMs - (112L * DAY_MS),
            sensitivityLevel = SensitivityLevel.HIGH_DISTURBANCE,
            triggerWarning = "Trigger Warning: Child sexual assault (POCSO) inside school premises and police apathy.",
            relevantLaws = listOf(
                "POCSO Act Sections 4, 6 & 21 (Aggravated Penetrative Assault & Failure to Report)",
                "BNS Section 65(2) (Rape of Minor Under 12)",
                "Bombay High Court School Safety Guidelines"
            ),
            sourceName = "Suraksha Permanent Archive • Maharashtra Desk",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Bombay_High_Court.jpg/960px-Bombay_High_Court.jpg",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            mediaCaption = "Bombay High Court Suo Motu Monitoring & SIT Probe into Badlapur School Safety Lapses",
            mediaAttachments = listOf(
                CaseMediaAttachment(
                    id = "media_mh_photo_1",
                    type = MediaAttachmentType.IMAGE,
                    url = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Bombay_High_Court.jpg/960px-Bombay_High_Court.jpg",
                    title = "Bombay High Court Suo Motu Hearing on Badlapur School Case",
                    caption = "Division bench monitoring SIT prosecution of school trustees under POCSO Section 21 and statewide school safety audits.",
                    sourceCredit = "High Court Legal Archive"
                ),
                CaseMediaAttachment(
                    id = "media_mh_video_1",
                    type = MediaAttachmentType.VIDEO,
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Bombay_High_Court.jpg/960px-Bombay_High_Court.jpg",
                    title = "SIT Briefing & High Court School Safety Directives",
                    caption = "Coverage of the Special Investigation Team report and mandatory police verification orders for school staff.",
                    sourceCredit = "Maharashtra Legal Desk",
                    durationLabel = "0:15 • Briefing Clip",
                    externalBroadcastUrl = "https://www.youtube.com/results?search_query=Badlapur+school+case+Bombay+High+Court+SIT+news"
                )
            ),
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_mh_1",
                    caseId = "case_mh_badlapur_school_02",
                    dateLabel = "Day 1–5 • Public Outrage & SIT Formation",
                    stageBadge = "FIR_FILED",
                    headline = "3 senior police officers suspended for 11-hour FIR delay; Special Investigation Team appointed",
                    details = "Following massive citizen protests at Badlapur railway station, the Maharashtra government suspended the station house officer and formed a special SIT.",
                    sourceName = "SIT Official Release"
                ),
                TimelineUpdateItem(
                    id = "upd_mh_2",
                    caseId = "case_mh_badlapur_school_02",
                    dateLabel = "Month 2 Follow-Up • High Court Monitoring",
                    stageBadge = "CHARGESHEET_FILED",
                    headline = "Bombay High Court reprimands police over slow arrest of school trustees",
                    details = "The High Court questioned why school management officials who concealed the crime were not immediately arrested under POCSO Section 21.",
                    sourceName = "High Court Legal Correspondent"
                ),
                TimelineUpdateItem(
                    id = "upd_mh_3",
                    caseId = "case_mh_badlapur_school_02",
                    dateLabel = "Month 3 Follow-Up • School Audit Check",
                    stageBadge = "TRIAL_ONGOING",
                    headline = "Trial against school trustees proceeds; 38% of Thane schools still lack female washroom attendants",
                    details = "Follow-up RTI and citizen audit in Badlapur and Thane revealed that despite government circulars, over a third of private schools had not completed background checks on support staff.",
                    sourceName = "Suraksha 90-Day Follow-Up Desk"
                )
            )
        ),
        CaseDossier(
            id = "case_ka_hubballi_campus_03",
            title = "Hubballi College Campus Attack: 23-Year-Old MCA Student Stabbed Inside College Premises in Broad Daylight",
            summary = "A 23-year-old postgraduate girl student was brutally stabbed multiple times inside her college campus in Hubballi, Karnataka, raising urgent questions on campus entry control and stalking intervention.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                A 23-year-old Master of Computer Applications (MCA) first-year student was attacked with a knife inside her college campus at Vidyanagar in Hubballi, Karnataka, right after stepping out of an examination hall.
                
                1. CAMPUS SECURITY FAILURE:
                The assailant entered the university campus unimpeded carrying a concealed blade and ambushed the student on the campus walkway. Despite daytime classes and hundreds of students being present, there were no panic buttons or armed security guards stationed near the examination block.
                
                2. CONTINUOUS FOLLOW-UP:
                This dossier tracks the fast-track trial in the Special Court, the implementation of the Karnataka Higher Education Council's campus safety mandate, and the constituency accountability of Hubballi-Dharwad representatives.
            """.trimIndent(),
            stateName = "Karnataka",
            districtName = "Hubballi-Dharwad",
            cityName = "Hubballi Campus Zone",
            mlaName = "Mahesh Tenginakai",
            mlaConstituency = "Hubli-Dharwad Central AC",
            mlaParty = "BJP",
            mpName = "Pralhad Joshi",
            mpConstituency = "Dharwad Lok Sabha",
            mpParty = "BJP",
            policeJurisdiction = "Hubballi-Dharwad Police Commissionerate • Vidyanagar PS",
            victimCategory = "Young Woman (College Student)",
            crimeCategory = "School/Campus Attack",
            caseStage = CaseStage.TRIAL_ONGOING,
            incidentDateLabel = "145 Days Ago • Tracked Monthly",
            incidentEpochMillis = nowMs - (145L * DAY_MS),
            sensitivityLevel = SensitivityLevel.HIGH_DISTURBANCE,
            triggerWarning = "Trigger Warning: Fatal knife attack on a female college student inside university premises.",
            relevantLaws = listOf(
                "BNS Section 103 (Murder)",
                "BNS Section 78 (Stalking)",
                "Arms Act Section 25"
            ),
            sourceName = "Suraksha Permanent Archive • Karnataka Desk",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/High_Court_of_Karnataka.jpg/960px-High_Court_of_Karnataka.jpg",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            mediaCaption = "Karnataka CID 483-Page Chargesheet & University Campus Security Audit in Hubballi-Dharwad",
            mediaAttachments = listOf(
                CaseMediaAttachment(
                    id = "media_ka_photo_1",
                    type = MediaAttachmentType.IMAGE,
                    url = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/High_Court_of_Karnataka.jpg/960px-High_Court_of_Karnataka.jpg",
                    title = "Karnataka Fast-Track Court & CID Investigation Record",
                    caption = "CID submitted a 483-page forensic chargesheet with 92 witnesses following the Hubballi college campus attack.",
                    sourceCredit = "Karnataka CID / Judicial Record"
                ),
                CaseMediaAttachment(
                    id = "media_ka_video_1",
                    type = MediaAttachmentType.VIDEO,
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/High_Court_of_Karnataka.jpg/960px-High_Court_of_Karnataka.jpg",
                    title = "Hubballi Campus Security Investigation & CID Briefing",
                    caption = "Official briefing on the fast-track trial and biometric campus entry reform demands.",
                    sourceCredit = "Karnataka Bureau",
                    durationLabel = "1:00 • News Clip",
                    externalBroadcastUrl = "https://www.youtube.com/results?search_query=Hubballi+college+campus+CID+chargesheet+news"
                )
            ),
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_ka_1",
                    caseId = "case_ka_hubballi_campus_03",
                    dateLabel = "Day 1 • Campus Arrest & Statewide Student Protests",
                    stageBadge = "ACCUSED_ARRESTED",
                    headline = "Assailant arrested within an hour; students across Karnataka demand mandatory campus biometric gates",
                    details = "Vidyanagar Police arrested the accused and transferred the investigation to the CID for fast-track prosecution.",
                    sourceName = "Karnataka CID"
                ),
                TimelineUpdateItem(
                    id = "upd_ka_2",
                    caseId = "case_ka_hubballi_campus_03",
                    dateLabel = "Month 3 Follow-Up • 483-Page CID Chargesheet",
                    stageBadge = "CHARGESHEET_FILED",
                    headline = "CID submits 483-page chargesheet with 92 witnesses; Special Fast-Track Court designated",
                    details = "The Criminal Investigation Department filed forensic, digital, and eyewitness evidence within the statutory window.",
                    sourceName = "Suraksha 90-Day Follow-Up Desk"
                ),
                TimelineUpdateItem(
                    id = "upd_ka_3",
                    caseId = "case_ka_hubballi_campus_03",
                    dateLabel = "Month 4 Follow-Up • Witness Examination",
                    stageBadge = "TRIAL_ONGOING",
                    headline = "Day-to-day witness hearings underway; family seeks permanent statutory law against stalking",
                    details = "The victim's parents met state and parliamentary representatives demanding non-bailable fast-track action on first-time stalking complaints by female students.",
                    sourceName = "Suraksha 120-Day Follow-Up Desk"
                )
            )
        ),
        CaseDossier(
            id = "case_wb_kolkata_hospital_04",
            title = "Kolkata Medical College Night-Shift Crime: Postgraduate Trainee Doctor Assaulted in Seminar Hall; Systemic Hospital Safety Audit",
            summary = "A 31-year-old postgraduate female doctor on a 36-hour shift was assaulted and murdered inside a government medical college seminar room in North Kolkata, exposing severe lack of safe duty rooms, CCTV, and security for women on night shifts.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                A postgraduate trainee doctor at R.G. Kar Medical College and Hospital in North Kolkata was found assaulted and murdered inside the chest department seminar hall after working a grueling 36-hour night shift without a secure on-call rest room.
                
                1. SYSTEMIC WORKPLACE VIOLENCE AGAINST WOMEN:
                The crime exposed nationwide neglect of basic safety infrastructure for female doctors, nurses, and night-shift workers—missing lockable duty rooms, broken CCTV cameras, dark hospital corridors, and unregulated entry of unauthorized individuals.
                
                2. LONG-TERM TRACKING:
                This permanent dossier tracks the CBI investigation, Supreme Court National Task Force compliance across state medical colleges, and constituency accountability in North Kolkata.
            """.trimIndent(),
            stateName = "West Bengal",
            districtName = "Kolkata",
            cityName = "Shyambazar / North Kolkata",
            mlaName = "Dr. Shashi Panja",
            mlaConstituency = "Shyampukur AC",
            mlaParty = "AITC",
            mpName = "Sudip Bandyopadhyay",
            mpConstituency = "Kolkata Uttar Lok Sabha",
            mpParty = "AITC",
            policeJurisdiction = "Kolkata Police Tala PS • Transferred to CBI",
            victimCategory = "Woman (Healthcare Worker)",
            crimeCategory = "Workplace & Campus Assault",
            caseStage = CaseStage.TRIAL_ONGOING,
            incidentDateLabel = "98 Days Ago • Tracked Monthly",
            incidentEpochMillis = nowMs - (98L * DAY_MS),
            sensitivityLevel = SensitivityLevel.HIGH_DISTURBANCE,
            triggerWarning = "Trigger Warning: Sexual assault and murder of a female doctor on night duty.",
            relevantLaws = listOf(
                "BNS Section 64 & 66 (Rape & Inflicting Injury Causing Death)",
                "BNS Section 103(1) (Murder)",
                "Supreme Court National Task Force Medical Safety Directives"
            ),
            sourceName = "Suraksha Permanent Archive • Eastern Bureau",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Calcutta_High_Court.jpg/960px-Calcutta_High_Court.jpg",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
            mediaCaption = "Calcutta High Court & Supreme Court National Task Force Audit of Night-Shift Medical Safety",
            mediaAttachments = listOf(
                CaseMediaAttachment(
                    id = "media_wb_photo_1",
                    type = MediaAttachmentType.IMAGE,
                    url = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Calcutta_High_Court.jpg/960px-Calcutta_High_Court.jpg",
                    title = "Calcutta High Court Order Transferring Probe to CBI",
                    caption = "Judicial oversight of the R.G. Kar Medical College investigation and mandatory hospital night-shift safety infrastructure.",
                    sourceCredit = "Eastern Legal Bureau"
                ),
                CaseMediaAttachment(
                    id = "media_wb_video_1",
                    type = MediaAttachmentType.VIDEO,
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
                    thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Calcutta_High_Court.jpg/960px-Calcutta_High_Court.jpg",
                    title = "Hospital Night-Shift Safety Audit & Sealdah Court Trial Report",
                    caption = "Tracking compliance with Supreme Court National Task Force directives on CCTV and secure duty rooms.",
                    sourceCredit = "Eastern Bureau Video",
                    durationLabel = "0:15 • Report Clip",
                    externalBroadcastUrl = "https://www.youtube.com/results?search_query=Kolkata+medical+college+Supreme+Court+safety+audit+news"
                )
            ),
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_wb_1",
                    caseId = "case_wb_kolkata_hospital_04",
                    dateLabel = "Week 1 • Calcutta High Court Transfers Probe to CBI",
                    stageBadge = "ACCUSED_ARRESTED",
                    headline = "High Court orders immediate handover of case diaries and forensic samples to CBI",
                    details = "Citing grave lapses by hospital administration in filing an unnatural death report instead of a prompt institutional FIR, the court transferred the probe.",
                    sourceName = "Calcutta High Court Order"
                ),
                TimelineUpdateItem(
                    id = "upd_wb_2",
                    caseId = "case_wb_kolkata_hospital_04",
                    dateLabel = "Month 2 Follow-Up • CBI Chargesheet & Evidence Tampering Arrests",
                    stageBadge = "CHARGESHEET_FILED",
                    headline = "CBI files primary chargesheet; former hospital principal & local SHO booked for evidence tampering",
                    details = "Investigation expanded to institutional cover-up and delayed crime-scene sealing.",
                    sourceName = "Suraksha 60-Day Follow-Up Desk"
                ),
                TimelineUpdateItem(
                    id = "upd_wb_3",
                    caseId = "case_wb_kolkata_hospital_04",
                    dateLabel = "Month 3 Follow-Up • In-Camera Sealdah Court Trial",
                    stageBadge = "TRIAL_ONGOING",
                    headline = "Daily in-camera trial begins at Sealdah Court; junior doctors audit hospital CCTV & duty room promises",
                    details = "Three months after the crime, citizen monitors verify whether promised panic buttons and separate female rest rooms have actually been completed.",
                    sourceName = "Suraksha 90-Day Follow-Up Desk"
                )
            )
        ),
        CaseDossier(
            id = "case_up_varanasi_campus_05",
            title = "Varanasi University Campus Night Assault: IIT-BHU Female Student Stripped & Filmed by 3 Men on Motorcycle Inside Campus",
            summary = "A 20-year-old undergraduate engineering student was waylaid at gunpoint inside the campus perimeter at night by three men on a motorcycle, assaulted, and threatened—yet the accused remained at large for 60 days until sustained student tracking forced arrests.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                A 20-year-old B.Tech student walking near Karman Baba temple inside the IIT-BHU campus in Varanasi was intercepted by three men riding a bullet motorcycle. She was separated from her friend, forcibly taken to a corner, stripped, filmed on a mobile phone, and threatened with death if she spoke out.
                
                1. THE 60-DAY ARREST DELAY & LATER BAIL CONTROVERSY:
                Despite hundreds of CCTV cameras across Varanasi and immediate student sit-in protests, police took 60 days to arrest the three accused. Eight months later, when media attention had completely moved on, two of the accused were quietly granted bail—highlighting why permanent public tracking after 1, 2, and 6+ months is vital.
            """.trimIndent(),
            stateName = "Uttar Pradesh",
            districtName = "Varanasi",
            cityName = "Lanka / BHU Campus Belt",
            mlaName = "Saurabh Srivastava",
            mlaConstituency = "Varanasi Cantt AC",
            mlaParty = "BJP",
            mpName = "Narendra Modi",
            mpConstituency = "Varanasi Lok Sabha",
            mpParty = "BJP",
            policeJurisdiction = "Varanasi Commissionerate • Lanka Police Station",
            victimCategory = "Teenage / College Student",
            crimeCategory = "School/Campus Attack",
            caseStage = CaseStage.TRIAL_ONGOING,
            incidentDateLabel = "180+ Days Ago • Long-Term Watch",
            incidentEpochMillis = nowMs - (185L * DAY_MS),
            sensitivityLevel = SensitivityLevel.SENSITIVE,
            triggerWarning = "Trigger Warning: Sexual assault, abduction threat, and non-consensual filming on college campus.",
            relevantLaws = listOf(
                "IPC Section 376D / BNS Section 70 (Gang Rape)",
                "IPC Section 354B / BNS Section 76 (Assault with Intent to Disrobe)",
                "IT Act Section 66E (Violation of Privacy)"
            ),
            sourceName = "Suraksha Permanent Archive • Uttar Pradesh Desk",
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_up_1",
                    caseId = "case_up_varanasi_campus_05",
                    dateLabel = "Day 1 • FIR Filed Without Gang-Rape Section Initially",
                    stageBadge = "FIR_FILED",
                    headline = "Students lock campus gates demanding addition of gang-rape provisions and boundary wall construction",
                    details = "Following the survivor's magistrate statement under Section 164 CrPC, gang-rape sections were added to the FIR.",
                    sourceName = "Lanka PS Record"
                ),
                TimelineUpdateItem(
                    id = "upd_up_2",
                    caseId = "case_up_varanasi_campus_05",
                    dateLabel = "Month 2 Follow-Up • Day 60 Arrests",
                    stageBadge = "ACCUSED_ARRESTED",
                    headline = "After 60 days of silence, police arrest all 3 accused and seize motorcycle used in crime",
                    details = "Sustained pressure from student collectives kept the case alive until arrests were made.",
                    sourceName = "Suraksha 60-Day Follow-Up Desk"
                ),
                TimelineUpdateItem(
                    id = "upd_up_3",
                    caseId = "case_up_varanasi_campus_05",
                    dateLabel = "Month 6 Follow-Up • Bail & Witness Protection Watch",
                    stageBadge = "TRIAL_ONGOING",
                    headline = "High Court grants conditional bail to two accused; legal advocates demand strict witness protection for survivor",
                    details = "Demonstrates why cases cannot be forgotten after arrests: trial cross-examination and survivor safety require continuous public vigilance.",
                    sourceName = "Suraksha 180-Day Follow-Up Desk"
                )
            )
        ),
        CaseDossier(
            id = "case_dl_shahbad_teen_06",
            title = "Delhi Shahbad Dairy Teenager Case: 16-Year-Old Girl Attacked in Public Lane as Bystanders Walked Past; Fast-Track Trial Watch",
            summary = "A 16-year-old teenage girl in North Delhi's Shahbad Dairy was attacked with a knife and stone on a busy street while bystanders failed to intervene, underscoring both stalking warning signs and public apathy.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                A 16-year-old minor girl walking to a friend's birthday gathering in Shahbad Dairy, Rohini (North Delhi), was cornered and brutally attacked by a 20-year-old man after she ended communication with him.
                
                1. BYSTANDER APATHY & PRIOR STALKING RED FLAGS:
                CCTV footage showed multiple passersby witnessing the assault without calling 112 or intervening to save the teenage girl.
                
                2. LONG-TERM JUDICIAL TRACKING:
                Tracked in the Permanent Vault to monitor witness testimony completion in the Rohini POCSO & Sessions Court and street-lighting/patrol reforms in North Delhi resettlement colonies.
            """.trimIndent(),
            stateName = "Delhi NCR",
            districtName = "North Delhi",
            cityName = "Shahbad Dairy / Rohini",
            mlaName = "Vijender Gupta",
            mlaConstituency = "Rohini AC",
            mlaParty = "BJP",
            mpName = "Yogendra Chandolia",
            mpConstituency = "North West Delhi Lok Sabha",
            mpParty = "BJP",
            policeJurisdiction = "Delhi Police • Outer North District (Shahbad Dairy PS)",
            victimCategory = "Teenage Student (13-19)",
            crimeCategory = "Weapon/Acid Attack",
            caseStage = CaseStage.TRIAL_ONGOING,
            incidentDateLabel = "160 Days Ago • Tracked Monthly",
            incidentEpochMillis = nowMs - (160L * DAY_MS),
            sensitivityLevel = SensitivityLevel.HIGH_DISTURBANCE,
            triggerWarning = "Trigger Warning: Extreme weapon violence against a 16-year-old minor girl and bystander inaction.",
            relevantLaws = listOf(
                "BNS Section 103 (Murder)",
                "POCSO Act & SC/ST Prevention of Atrocities Act",
                "Arms Act"
            ),
            sourceName = "Suraksha Permanent Archive • Delhi NCR Bureau",
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_dl_1",
                    caseId = "case_dl_shahbad_teen_06",
                    dateLabel = "Day 2 • Accused Arrested from Bulandshahr",
                    stageBadge = "ACCUSED_ARRESTED",
                    headline = "Accused tracked via technical surveillance to UP and remanded; weapon recovered",
                    details = "Delhi Police recovered the knife and clothes worn during the crime.",
                    sourceName = "Delhi Police Crime Branch"
                ),
                TimelineUpdateItem(
                    id = "upd_dl_2",
                    caseId = "case_dl_shahbad_teen_06",
                    dateLabel = "Month 1 Follow-Up • 640-Page Chargesheet",
                    stageBadge = "CHARGESHEET_FILED",
                    headline = "640-page chargesheet with FSL DNA and CCTV evidence filed before Rohini Court",
                    details = "Forensic and digital chat records included to prove premeditated stalking.",
                    sourceName = "Suraksha 30-Day Follow-Up Desk"
                ),
                TimelineUpdateItem(
                    id = "upd_dl_3",
                    caseId = "case_dl_shahbad_teen_06",
                    dateLabel = "Month 5 Follow-Up • Survivor Family Compensation & Trial",
                    stageBadge = "TRIAL_ONGOING",
                    headline = "Key prosecution witnesses examined; legal aid monitors interim victim compensation disbursement",
                    details = "Ensuring the minor victim's younger siblings receive promised educational and housing support.",
                    sourceName = "Suraksha 150-Day Follow-Up Desk"
                )
            )
        ),
        CaseDossier(
            id = "case_rj_kota_hostel_07",
            title = "Kota Coaching Belt Teen Abduction & Cyber Extortion Case: 17-Year-Old NEET Aspirant Targeted via Hostel Security Lapse",
            summary = "A 17-year-old girl preparing for competitive exams in Kota, Rajasthan, was abducted and subjected to extortion threats after her unregistered PG hostel failed to log nighttime entry/exit or alert parents.",
            fullReport = """
                INCIDENT DOSSIER • PERMANENT ARCHIVE RECORD
                
                Thousands of teenage girls move to Kota, Rajasthan every year for medical and engineering coaching. In this tracked incident, a 17-year-old minor girl was targeted by an organized cyber-stalking and abduction ring operating near unregistered private hostels.
                
                1. WHY THIS CASE IS ARCHIVED:
                Tracks enforcement of the Kota District Administration's mandatory Hostel & PG Safety Guidelines, biometric attendance alerts for minor girls, and prosecution of the extortion gang.
            """.trimIndent(),
            stateName = "Rajasthan",
            districtName = "Kota",
            cityName = "Kota Coaching Belt",
            mlaName = "Sandeep Sharma",
            mlaConstituency = "Kota South AC",
            mlaParty = "BJP",
            mpName = "Om Birla",
            mpConstituency = "Kota Lok Sabha",
            mpParty = "BJP",
            policeJurisdiction = "Kota City Police • Vigyan Nagar PS",
            victimCategory = "Teenage Student (13-19)",
            crimeCategory = "Cyber Harassment & Abduction",
            caseStage = CaseStage.CHARGESHEET_FILED,
            incidentDateLabel = "54 Days Ago • Tracked Monthly",
            incidentEpochMillis = nowMs - (54L * DAY_MS),
            sensitivityLevel = SensitivityLevel.SENSITIVE,
            triggerWarning = "Trigger Warning: Abduction and cyber blackmail of a 17-year-old minor student.",
            relevantLaws = listOf(
                "BNS Section 137 (Kidnapping of Minor)",
                "POCSO Act Section 12",
                "IT Act Section 66E & BNS Section 308 (Extortion)"
            ),
            sourceName = "Suraksha Permanent Archive • Rajasthan Desk",
            timelineUpdates = listOf(
                TimelineUpdateItem(
                    id = "upd_rj_1",
                    caseId = "case_rj_kota_hostel_07",
                    dateLabel = "Day 4 • Special Police Team Rescues Student & Arrests 3",
                    stageBadge = "ACCUSED_ARRESTED",
                    headline = "Multi-state police team traces suspects; unregistered PG hostel sealed",
                    details = "Kota Police rescued the minor student and sealed two hostels operating without women wardens.",
                    sourceName = "Kota City Police"
                ),
                TimelineUpdateItem(
                    id = "upd_rj_2",
                    caseId = "case_rj_kota_hostel_07",
                    dateLabel = "Month 1 Follow-Up • Digital Forensics & Chargesheet",
                    stageBadge = "CHARGESHEET_FILED",
                    headline = "Chargesheet filed; 42 coaching hostels served show-cause notices for missing CCTV and female security",
                    details = "Follow-up audit tracks how many hostels actually complied after the 30-day deadline.",
                    sourceName = "Suraksha 30-Day Follow-Up Desk"
                )
            )
        )
    )

    val emergencyHelplines: List<EmergencyHelpline> = listOf(
        EmergencyHelpline(
            id = "help_112",
            number = "112",
            title = "National Emergency Response (ERSS)",
            subtitle = "Immediate police, ambulance, and PCR van dispatch with GPS location tracking across all Indian states.",
            availability = "24x7 • All India Toll-Free",
            targetGroup = "All Girls, Teenagers & Women"
        ),
        EmergencyHelpline(
            id = "help_1098",
            number = "1098",
            title = "CHILDLINE India (Schoolgirls & Teens <18)",
            subtitle = "Dedicated emergency rescue, POCSO intervention, school safety breach reporting, and shelter for girls under 18.",
            availability = "24x7 • Ministry of Women & Child Development",
            targetGroup = "Minor Girls & Teenage Students"
        ),
        EmergencyHelpline(
            id = "help_1091",
            number = "1091",
            title = "Women in Distress Police Helpline",
            subtitle = "Direct connection to District Women Police Cells, Nirbhaya/Disha squads, and stalking/assault response units.",
            availability = "24x7 • State Police Control Rooms",
            targetGroup = "Women & College Students"
        ),
        EmergencyHelpline(
            id = "help_181",
            number = "181",
            title = "Abhayam / One-Stop Women Helpline",
            subtitle = "Connects survivors to One-Stop Sakhi Centres for medical aid, legal counsel, FIR assistance, and temporary shelter.",
            availability = "24x7 • National Toll-Free",
            targetGroup = "Domestic, Dowry & Assault Survivors"
        ),
        EmergencyHelpline(
            id = "help_1930",
            number = "1930",
            title = "National Cyber Crime Helpline",
            subtitle = "Immediate takedown & FIR support for non-consensual photos, deepfake morphing, cyber stalking, and blackmail.",
            availability = "24x7 • Indian Cyber Crime Coordination Centre",
            targetGroup = "Teenagers & Women Facing Cyber Abuse"
        ),
        EmergencyHelpline(
            id = "help_ncw",
            number = "7827170170",
            title = "National Commission for Women (NCW)",
            subtitle = "24x7 distress helpline and cognisance portal when local police delay or refuse to register an FIR.",
            availability = "24x7 • Statutory National Commission",
            targetGroup = "Police Apathy & Institutional Complaints"
        )
    )

    val legalRightsGuides: List<LegalRightGuide> = listOf(
        LegalRightGuide(
            id = "law_zero_fir",
            codeBadge = "BNSS SECTION 173 • ZERO FIR",
            title = "Right to File a Zero FIR at Any Police Station in India",
            summary = "No police officer can refuse to register a crime against a girl or woman by claiming the incident happened outside their territorial jurisdiction.",
            actionChecklist = listOf(
                "Demand registration of a Zero FIR at the nearest police station regardless of which city or district the crime occurred in.",
                "For sexual assault, stalking, or acid/weapon attacks, the statement MUST be recorded by a woman police officer.",
                "If a police officer refuses to record a cognizable offence against a woman, they face 6 months to 2 years rigorous imprisonment under BNS Section 199.",
                "You are legally entitled to a free copy of the FIR immediately upon registration."
            ),
            statutoryCitation = "Bharatiya Nagarik Suraksha Sanhita (BNSS), 2023 — Section 173(1) & BNS Section 199"
        ),
        LegalRightGuide(
            id = "law_pocso_school",
            codeBadge = "POCSO ACT • MINOR & SCHOOL SAFETY",
            title = "Protection of Schoolgirls & Teenagers Under 18 (POCSO Act)",
            summary = "Strict legal mandates when a crime, stalking, or security breach occurs against a girl under 18 in a school, hostel, or tuition center.",
            actionChecklist = listOf(
                "Mandatory Reporting (Section 19 & 21): Any school principal, management trustee, or staff member who fails to report an offence or attempts to cover it up commits a punishable crime (up to 6 months–1 year imprisonment).",
                "Child-Friendly Recording: A minor girl's statement must be recorded at her residence or a place of her choice by a woman officer in plain clothes—never by detaining her at a police station.",
                "Identity Protection (Section 23): Disclosing the name, school, photograph, or family details of a minor victim in media or social networks is strictly prohibited.",
                "Special POCSO Court: Trial must be completed in a child-friendly Special Court, ideally within 1 year of cognizance."
            ),
            statutoryCitation = "Protection of Children from Sexual Offences (POCSO) Act, 2012 — Sections 11, 19, 21, 23, 24 & 35"
        ),
        LegalRightGuide(
            id = "law_stalking_weapons",
            codeBadge = "BNS SECTIONS 78, 109 & 124",
            title = "Stalking, Weapon Attacks & Acid Attacks Against Girls & Women",
            summary = "Physical or digital following, classroom/campus intrusion, and attacks with weapons or corrosive substances carry severe punishments.",
            actionChecklist = listOf(
                "Preserve all digital evidence (call logs, WhatsApp messages, Instagram threats, and eyewitness contacts) without deleting threads.",
                "First-time stalking conviction under BNS Section 78 carries up to 3 years imprisonment; repeat offences carry up to 5 years.",
                "Weapon or chainsaw/knife attacks intending to kill are prosecuted under BNS Section 109 (Attempt to Murder — up to life imprisonment).",
                "All public and private hospitals are legally bound (BNS Section 200) to provide immediate, 100% free medical first-aid and treatment to survivors without waiting for police arrival."
            ),
            statutoryCitation = "Bharatiya Nyaya Sanhita (BNS), 2023 — Sections 78, 109, 124 & 200"
        ),
        LegalRightGuide(
            id = "law_mp_mla_accountability",
            codeBadge = "CIVIC ACCOUNTABILITY • MP & MLA",
            title = "Holding Your Area's MLA & MP Accountable for Unresolved Cases",
            summary = "How citizens and parents can formally escalate stalled investigations and campus safety lapses to elected constituency representatives.",
            actionChecklist = listOf(
                "District Vigilance & Monitoring: Every MP and MLA can raise unstarred/starred questions in Parliament or the State Assembly regarding pending crimes against women in their constituency.",
                "Nirbhaya Fund & MPLADS/MLALADS: Citizens can demand allocation of local area development funds for street lighting, school CCTV gates, and pink patrol booths.",
                "Submit a formal dated memorandum citing the FIR number and 'Days Since Incident' to both the District Collector/SP and the Constituency MLA/MP office.",
                "Use Suraksha Chronicle's 'Save for Life' Dossier to track whether the representative issued an official follow-up after 30, 60, and 90 days."
            ),
            statutoryCitation = "Parliamentary & State Legislative Oversight Norms + District Level Crime Review Protocol"
        )
    )
}
