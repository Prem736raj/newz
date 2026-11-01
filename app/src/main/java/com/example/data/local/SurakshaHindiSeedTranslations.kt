package com.example.data.local

import com.example.data.model.CaseDossier
import com.example.data.model.EmergencyHelpline
import com.example.data.model.LegalRightGuide
import com.example.data.model.TimelineUpdateItem
import com.example.data.model.TranslationCacheEntity

/**
 * Verified, human-reviewed Hindi translations for all seed cases, chronological timeline updates,
 * emergency helplines, and legal rights guides so Hindi works offline and on first switch without delay.
 * Preserves MP and MLA names as officially published while translating titles, summaries, full reports,
 * timeline milestones, helplines, and statutory checklists.
 */
object SurakshaHindiSeedTranslations {

    data class TranslatedCaseFields(
        val title: String,
        val summary: String,
        val fullReport: String,
        val policeJurisdiction: String,
        val victimCategory: String,
        val crimeCategory: String,
        val incidentDateLabel: String,
        val triggerWarning: String,
        val relevantLaws: List<String>,
        val sourceName: String
    )

    data class TranslatedTimelineFields(
        val dateLabel: String,
        val headline: String,
        val details: String,
        val sourceName: String
    )

    data class TranslatedHelplineFields(
        val title: String,
        val subtitle: String,
        val availability: String,
        val targetGroup: String
    )

    data class TranslatedLegalGuideFields(
        val codeBadge: String,
        val title: String,
        val summary: String,
        val actionChecklist: List<String>,
        val statutoryCitation: String
    )

    val hindiCaseMap: Map<String, TranslatedCaseFields> = mapOf(
        "case_ap_school_chainsaw_01" to TranslatedCaseFields(
            title = "आंध्र प्रदेश स्कूल सुरक्षा चूक: 21 वर्षीय युवक ने कक्षा के भीतर 10वीं की छात्रा पर मैकेनिकल चेनसॉ से किया हमला",
            summary = "आंध्र प्रदेश के अनकापल्ली में एक गंभीर सुरक्षा चूक सामने आई है, जहां 21 वर्षीय युवक मैकेनिकल चेनसॉ लेकर स्कूल के गेट से अंदर घुस गया और पीछा करने (स्टॉकिंग) का विरोध करने पर 15 वर्षीय कक्षा 10 की छात्रा पर उसकी ही कक्षा में हमला कर दिया।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                आंध्र प्रदेश में स्कूल सुरक्षा की एक अत्यंत गंभीर चूक ने पूरे राज्य को झकझोर दिया जब एक 21 वर्षीय युवक हाई स्कूल परिसर में घुस गया और डरे हुए छात्रों व शिक्षकों के सामने कक्षा 10 की नाबालिग छात्रा पर मैकेनिकल चेनसॉ (आरी) से हमला कर दिया।
                
                1. सुरक्षा में चूक कैसे हुई:
                पुलिस जांच और चश्मदीद छात्रों के बयानों के अनुसार, 21 वर्षीय आरोपी कई हफ्तों से कक्षा 10 की छात्रा का पीछा कर रहा था और उसे परेशान कर रहा था। पूर्व चेतावनी के बावजूद, हमलावर बैग में भारी मैकेनिकल चेनसॉ छिपाकर स्कूल परिसर में घुसने में कामयाब रहा और गेट सुरक्षा द्वारा रोके बिना सीधे शैक्षणिक ब्लॉक में पहुंच गया।
                
                2. कक्षा में हमला और शिक्षकों का हस्तक्षेप:
                कक्षा 10 में घुसकर हमलावर ने चेनसॉ चालू किया और छात्रा पर वार किया, जिससे खुद को बचाने की कोशिश में छात्रा की गर्दन, कंधे और हाथ में गंभीर चोटें आईं। चीख-पुकार सुनकर शिक्षकों और वरिष्ठ कर्मचारियों ने तुरंत अंदर दौड़कर हमलावर को काबू में किया, हथियार जब्त किया और घायल छात्रा को आपातकालीन ट्रॉमा सेंटर पहुंचाया।
                
                3. इस मामले की निरंतर निगरानी क्यों आवश्यक है:
                अक्सर 48 घंटों के बाद सुर्खियां थम जाती हैं और पीड़ित परिवार अकेले लंबी कानूनी लड़ाई लड़ते रह जाते हैं। यह डॉसियर हर महीने की प्रगति को ट्रैक करता है: पीड़िता का उपचार, POCSO और BNS धारा 109 (हत्या का प्रयास) के तहत फास्ट-ट्रैक सुनवाई, आंध्र प्रदेश के स्कूलों में अनिवार्य सुरक्षा ऑडिट, और क्षेत्र के विधायक व सांसद की सीधी जवाबदेही।
            """.trimIndent(),
            policeJurisdiction = "अनकापल्ली जिला पुलिस • दिशा महिला थाना",
            victimCategory = "किशोर छात्रा (13-19 वर्ष)",
            crimeCategory = "स्कूल / परिसर हमला",
            incidentDateLabel = "76 दिन पूर्व • मासिक ट्रैकिंग",
            triggerWarning = "संवेदनशील चेतावनी: कक्षा के भीतर कक्षा 10 की नाबालिग छात्रा पर हथियार (मैकेनिकल चेनसॉ) से हमला।",
            relevantLaws = listOf(
                "BNS धारा 109 (हत्या का प्रयास)",
                "POCSO अधिनियम धारा 11 व 12 (नाबालिग का पीछा और हमला)",
                "BNS धारा 78 (स्टॉकिंग / पीछा करना)",
                "आंध्र प्रदेश दिशा विशेष न्यायालय फास्ट-ट्रैक निर्देश"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • आंध्र प्रदेश ब्यूरो"
        ),
        "case_mh_badlapur_school_02" to TranslatedCaseFields(
            title = "बदलापुर स्कूल नाबालिग उत्पीड़न मामला: स्कूल वॉशरूम में 4 वर्षीय दो बच्चियों के साथ यौन उत्पीड़न; संस्थागत लीपापोती पर नज़र",
            summary = "महाराष्ट्र के ठाणे जिले के बदलापुर में स्कूल परिसर के अंदर एक सफाई कर्मचारी द्वारा दो नाबालिग बच्चियों का यौन उत्पीड़न किया गया। पुलिस द्वारा FIR दर्ज करने में 11 घंटे से अधिक की देरी के बाद व्यापक नागरिक विरोध प्रदर्शन हुए।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                महाराष्ट्र के ठाणे जिले के बदलापुर स्थित एक प्रमुख स्कूल में किंडरगार्टन में पढ़ने वाली चार साल की दो बच्चियों के साथ बिना पुलिस सत्यापन के रखे गए एक पुरुष सफाई कर्मी द्वारा यौन उत्पीड़न किया गया।
                
                1. संस्थागत विफलता और FIR में 11 घंटे की देरी:
                जब पीड़ित माता-पिता ने स्कूल प्रबंधन और स्थानीय पुलिस स्टेशन से संपर्क किया, तो स्कूल ने कथित तौर पर अपनी प्रतिष्ठा बचाने के लिए मामले को दबाने का प्रयास किया, और स्थानीय पुलिस अधिकारी ने ज़ीरो/POCSO FIR दर्ज करने से पहले माता-पिता को 11 घंटे से अधिक समय तक इंतजार कराया।
                
                2. दीर्घकालिक जवाबदेही और हमारी निगरानी:
                विरोध प्रदर्शन शांत होने के महीनों बाद भी POCSO धारा 21 (रिपोर्ट न करने का अपराध) के तहत स्कूल ट्रस्टियों पर मुकदमा चलाने और बॉम्बे हाई कोर्ट के अनिवार्य स्कूल सुरक्षा समिति निर्देशों के पालन पर निगरानी जारी है।
            """.trimIndent(),
            policeJurisdiction = "ठाणे शहर पुलिस • बदलापुर पूर्व थाना / SIT",
            victimCategory = "स्कूली बच्ची / नाबालिग (<12 वर्ष)",
            crimeCategory = "POCSO / नाबालिग उत्पीड़न",
            incidentDateLabel = "112 दिन पूर्व • मासिक ट्रैकिंग",
            triggerWarning = "संवेदनशील चेतावनी: स्कूल परिसर में बाल यौन उत्पीड़न (POCSO) और पुलिस की संवेदनहीनता।",
            relevantLaws = listOf(
                "POCSO अधिनियम धारा 4, 6 व 21 (गंभीर यौन हमला एवं रिपोर्ट करने में विफलता)",
                "BNS धारा 65(2) (12 वर्ष से कम आयु की नाबालिग से अपराध)",
                "बॉम्बे उच्च न्यायालय स्कूल सुरक्षा दिशानिर्देश"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • महाराष्ट्र डेस्क"
        ),
        "case_ka_hubballi_campus_03" to TranslatedCaseFields(
            title = "हुबली कॉलेज परिसर हमला: दिनदहाड़े कॉलेज परिसर के अंदर 23 वर्षीय MCA छात्रा की चाकू मारकर हत्या",
            summary = "कर्नाटक के हुबली में अपने कॉलेज परिसर के भीतर 23 वर्षीय स्नातकोत्तर (MCA) छात्रा पर दिनदहाड़े चाकू से कई वार किए गए, जिससे कॉलेज प्रवेश नियंत्रण और स्टॉकिंग की शिकायतों पर कार्रवाई को लेकर गंभीर सवाल खड़े हुए।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                कर्नाटक के हुबली स्थित विद्यानगर में एक कॉलेज परिसर के अंदर परीक्षा हॉल से बाहर निकलते ही 23 वर्षीय मास्टर ऑफ कंप्यूटर एप्लीकेशन (MCA) प्रथम वर्ष की छात्रा पर चाकू से जानलेवा हमला किया गया।
                
                1. कैंपस सुरक्षा की विफलता:
                हमलावर छुपा हुआ हथियार लेकर बिना किसी रोक-टोक के विश्वविद्यालय परिसर में दाखिल हुआ और रास्ते में छात्रा पर हमला कर दिया। दिन की कक्षाओं और सैकड़ों छात्रों की मौजूदगी के बावजूद परीक्षा ब्लॉक के पास कोई पैनिक बटन या प्रशिक्षित सुरक्षाकर्मी तैनात नहीं था।
                
                2. निरंतर फ़ॉलो-अप:
                यह डॉसियर विशेष न्यायालय में फास्ट-ट्रैक ट्रायल, कर्नाटक उच्च शिक्षा परिषद के कैंपस सुरक्षा आदेश के क्रियान्वयन और हुबली-धारवाड़ के जनप्रतिनिधियों की जवाबदेही को ट्रैक करता है।
            """.trimIndent(),
            policeJurisdiction = "हुबली-धारवाड़ पुलिस कमिश्नरेट • विद्यानगर थाना",
            victimCategory = "युवती (कॉलेज छात्रा)",
            crimeCategory = "स्कूल / परिसर हमला",
            incidentDateLabel = "145 दिन पूर्व • मासिक ट्रैकिंग",
            triggerWarning = "संवेदनशील चेतावनी: विश्वविद्यालय परिसर के भीतर कॉलेज छात्रा पर घातक हथियार से हमला।",
            relevantLaws = listOf(
                "BNS धारा 103 (हत्या)",
                "BNS धारा 78 (स्टॉकिंग / पीछा करना)",
                "शस्त्र अधिनियम (Arms Act) धारा 25"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • कर्नाटक डेस्क"
        ),
        "case_wb_kolkata_hospital_04" to TranslatedCaseFields(
            title = "कोलकाता मेडिकल कॉलेज नाइट-शिफ्ट अपराध: सेमिनार हॉल में स्नातकोत्तर ट्रेनी डॉक्टर पर हमला; अस्पताल सुरक्षा ऑडिट",
            summary = "उत्तरी कोलकाता के एक सरकारी मेडिकल कॉलेज के सेमिनार हॉल में 36 घंटे की ड्यूटी पर तैनात 31 वर्षीय महिला ट्रेनी डॉक्टर के साथ जघन्य अपराध और हत्या ने रात्रि पाली में काम करने वाली महिलाओं के लिए सुरक्षित ड्यूटी रूम और सीसीटीवी के गंभीर अभाव को उजागर किया।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                उत्तरी कोलकाता के आर.जी. कर मेडिकल कॉलेज एवं अस्पताल में 36 घंटे की कठिन रात्रि ड्यूटी के बाद सुरक्षित विश्राम कक्ष न होने के कारण चेस्ट विभाग के सेमिनार हॉल में एक स्नातकोत्तर ट्रेनी डॉक्टर के साथ जघन्य हमला और हत्या की गई।
                
                1. कार्यस्थल पर महिला सुरक्षा की प्रणालीगत विफलता:
                इस अपराध ने देशभर में महिला डॉक्टरों, नर्सों और रात्रि पाली के कर्मचारियों के लिए बुनियादी सुरक्षा ढांचे—तालाबंद ड्यूटी रूम, कार्यशील सीसीटीवी कैमरे, रोशनीयुक्त गलियारे और अनधिकृत व्यक्तियों के प्रवेश पर रोक—के अभाव को उजागर किया।
                
                2. दीर्घकालिक निगरानी:
                यह स्थायी डॉसियर CBI जांच, सर्वोच्च न्यायालय की नेशनल टास्क फोर्स के निर्देशों के अनुपालन और उत्तरी कोलकाता क्षेत्राधिकार की जवाबदेही को ट्रैक करता है।
            """.trimIndent(),
            policeJurisdiction = "कोलकाता पुलिस टाला थाना • CBI को हस्तांतरित",
            victimCategory = "महिला (स्वास्थ्यकर्मी / डॉक्टर)",
            crimeCategory = "कार्यस्थल एवं परिसर हमला",
            incidentDateLabel = "98 दिन पूर्व • मासिक ट्रैकिंग",
            triggerWarning = "संवेदनशील चेतावनी: रात्रि ड्यूटी पर तैनात महिला डॉक्टर के साथ जघन्य अपराध और हत्या।",
            relevantLaws = listOf(
                "BNS धारा 64 व 66 (दुष्कर्म एवं मृत्यु कारित करने वाली चोट)",
                "BNS धारा 103(1) (हत्या)",
                "सर्वोच्च न्यायालय नेशनल टास्क फोर्स चिकित्सा सुरक्षा निर्देश"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • पूर्वी ब्यूरो"
        ),
        "case_up_varanasi_campus_05" to TranslatedCaseFields(
            title = "वाराणसी विश्वविद्यालय परिसर रात्रि हमला: IIT-BHU की छात्रा को परिसर के भीतर मोटरसाइकिल सवार 3 युवकों ने घेरा; न्यायिक निगरानी",
            summary = "वाराणसी में विश्वविद्यालय परिसर के भीतर रात में 20 वर्षीय इंजीनियरिंग छात्रा को मोटरसाइकिल सवार तीन युवकों ने हथियार के बल पर रोका, प्रताड़ित किया और धमकाया—आरोपियों की गिरफ्तारी में 60 दिन लगे और बाद में जमानत पर रिहाई ने दीर्घकालिक ट्रैकिंग की आवश्यकता को सिद्ध किया।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                वाराणसी के IIT-BHU परिसर में करमन बाबा मंदिर के पास टहल रही 20 वर्षीय बी.टेक छात्रा को बुलेट मोटरसाइकिल सवार तीन युवकों ने रोक लिया। उसे उसके मित्र से अलग कर एक कोने में ले जाया गया, अभद्रता करते हुए मोबाइल से वीडियो बनाया गया और शिकायत करने पर जान से मारने की धमकी दी गई।
                
                1. गिरफ्तारी में 60 दिन की देरी और जमानत विवाद:
                वाराणसी में सैकड़ों सीसीटीवी कैमरों और छात्रों के तत्काल धरना-प्रदर्शन के बावजूद पुलिस को तीनों आरोपियों को गिरफ्तार करने में 60 दिन लग गए। आठ महीने बाद जब मीडिया का ध्यान हट गया, तो दो आरोपियों को जमानत मिल गई—जो दर्शाता है कि 1, 2 और 6 महीने बाद भी स्थायी जनहित ट्रैकिंग क्यों जरूरी है।
            """.trimIndent(),
            policeJurisdiction = "वाराणसी कमिश्नरेट • लंका पुलिस स्टेशन",
            victimCategory = "किशोरी / कॉलेज छात्रा",
            crimeCategory = "स्कूल / परिसर हमला",
            incidentDateLabel = "180+ दिन पूर्व • दीर्घकालिक निगरानी",
            triggerWarning = "संवेदनशील चेतावनी: कॉलेज परिसर में यौन उत्पीड़न, अपहरण की धमकी और जबरन वीडियो बनाना।",
            relevantLaws = listOf(
                "IPC धारा 376D / BNS धारा 70 (सामूहिक दुष्कर्म)",
                "IPC धारा 354B / BNS धारा 76 (निर्वास्त्र करने के आशय से हमला)",
                "IT अधिनियम धारा 66E (गोपनीयता का उल्लंघन)"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • उत्तर प्रदेश डेस्क"
        ),
        "case_dl_shahbad_teen_06" to TranslatedCaseFields(
            title = "दिल्ली शाहबाद डेयरी किशोरी मामला: सार्वजनिक गली में 16 वर्षीय लड़की पर हमला, मूकदर्शक बने रहे राहगीर; फास्ट-ट्रैक ट्रायल",
            summary = "उत्तरी दिल्ली के शाहबाद डेयरी में एक व्यस्त सड़क पर 16 वर्षीय किशोरी पर चाकू और पत्थर से हमला किया गया जबकि राहगीर बिना मदद किए गुजरते रहे। यह मामला स्टॉकिंग की पूर्व चेतावनियों और सार्वजनिक उदासीनता दोनों को रेखांकित करता है।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                रोहिणी (उत्तरी दिल्ली) के शाहबाद डेयरी में एक सहेली के जन्मदिन में जा रही 16 वर्षीय नाबालिग लड़की को 20 वर्षीय युवक ने बातचीत बंद करने से नाराज होकर गली में घेर लिया और बेरहमी से हमला किया।
                
                1. राहगीरों की उदासीनता और स्टॉकिंग के संकेत:
                सीसीटीवी फुटेज में दिखा कि कई राहगीर बिना 112 पर कॉल किए या किशोरी को बचाने का प्रयास किए वहां से गुजर गए।
                
                2. दीर्घकालिक न्यायिक ट्रैकिंग:
                रोहिणी POCSO एवं सत्र न्यायालय में गवाहों की गवाही पूरी होने और उत्तरी दिल्ली की पुनर्वास कॉलोनियों में स्ट्रीट लाइटिंग व पुलिस गश्त सुधारों की निगरानी के लिए यह मामला स्थायी अभिलेखागार में दर्ज है।
            """.trimIndent(),
            policeJurisdiction = "दिल्ली पुलिस • बाहरी उत्तरी जिला (शाहबाद डेयरी थाना)",
            victimCategory = "किशोर छात्रा (13-19 वर्ष)",
            crimeCategory = "हथियार / एसिड हमला",
            incidentDateLabel = "160 दिन पूर्व • मासिक ट्रैकिंग",
            triggerWarning = "संवेदनशील चेतावनी: 16 वर्षीय नाबालिग लड़की पर हथियार से गंभीर हिंसा और राहगीरों की निष्क्रियता।",
            relevantLaws = listOf(
                "BNS धारा 103 (हत्या)",
                "POCSO अधिनियम एवं SC/ST अत्याचार निवारण अधिनियम",
                "शस्त्र अधिनियम (Arms Act)"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • दिल्ली NCR ब्यूरो"
        ),
        "case_rj_kota_hostel_07" to TranslatedCaseFields(
            title = "कोटा कोचिंग क्षेत्र नाबालिग अपहरण और साइबर वसूली मामला: हॉस्टल सुरक्षा चूक से 17 वर्षीय NEET छात्रा बनी निशाना",
            summary = "राजस्थान के कोटा में प्रतियोगी परीक्षा की तैयारी कर रही 17 वर्षीय छात्रा का अपहरण कर फिरौती व साइबर ब्लैकमेलिंग की गई, क्योंकि उसके गैर-पंजीकृत पीजी हॉस्टल ने रात्रि प्रवेश/निकास दर्ज नहीं किया और न ही अभिभावकों को सूचित किया।",
            fullReport = """
                घटना डॉसियर • स्थायी अभिलेखागार रिकॉर्ड
                
                हर साल हजारों किशोरियां मेडिकल और इंजीनियरिंग कोचिंग के लिए कोटा, राजस्थान आती हैं। इस मामले में, गैर-पंजीकृत निजी हॉस्टलों के पास सक्रिय एक संगठित साइबर-स्टॉकिंग और अपहरण गिरोह ने 17 वर्षीय नाबालिग छात्रा को निशाना बनाया।
                
                1. यह मामला क्यों संग्रहीत है:
                कोटा जिला प्रशासन के अनिवार्य हॉस्टल एवं पीजी सुरक्षा दिशानिर्देशों, नाबालिग छात्राओं के लिए बायोमेट्रिक उपस्थिति अलर्ट और वसूली गिरोह पर मुकदमे की निगरानी के लिए इसे ट्रैक किया जा रहा है।
            """.trimIndent(),
            policeJurisdiction = "कोटा शहर पुलिस • विज्ञान नगर थाना",
            victimCategory = "किशोर छात्रा (13-19 वर्ष)",
            crimeCategory = "साइबर उत्पीड़न और अपहरण",
            incidentDateLabel = "54 दिन पूर्व • मासिक ट्रैकिंग",
            triggerWarning = "संवेदनशील चेतावनी: 17 वर्षीय नाबालिग छात्रा का अपहरण और साइबर ब्लैकमेल।",
            relevantLaws = listOf(
                "BNS धारा 137 (नाबालिग का अपहरण)",
                "POCSO अधिनियम धारा 12",
                "IT अधिनियम धारा 66E एवं BNS धारा 308 (जबरन वसूली)"
            ),
            sourceName = "सुरक्षा स्थायी अभिलेखागार • राजस्थान डेस्क"
        )
    )

    val hindiTimelineMap: Map<String, TranslatedTimelineFields> = mapOf(
        "upd_ap_1" to TranslatedTimelineFields(
            dateLabel = "दिन 1 • घटना और तत्काल गिरफ़्तारी",
            headline = "कक्षा 10 के भीतर हमलावर को काबू किया गया; छात्रा को ट्रॉमा ICU में भर्ती कराया गया",
            details = "पुलिस ने परिसर में मैकेनिकल चेनसॉ जब्त किया, BNS धारा 109 (हत्या का प्रयास) और POCSO अधिनियम के तहत FIR दर्ज की और 21 वर्षीय आरोपी को न्यायिक हिरासत में भेज दिया।",
            sourceName = "जिला पुलिस बुलेटिन"
        ),
        "upd_ap_2" to TranslatedTimelineFields(
            dateLabel = "माह 1 फ़ॉलो-अप • दिन 32",
            headline = "जमानत याचिका खारिज; जिला शिक्षा अधिकारी (DEO) की जांच में स्कूल गेट पर प्रशिक्षित गार्ड न होने का खुलासा",
            details = "राष्ट्रीय टीवी से खबरें गायब होने के एक महीने बाद, जिला शिक्षा अधिकारी ने जांच रिपोर्ट सौंपी जिसमें पुष्टि हुई कि स्कूल में कार्यशील सीसीटीवी और गेट जांच नहीं थी। पीड़िता की दूसरी रिकंस्ट्रक्टिव सर्जरी हुई।",
            sourceName = "सुरक्षा 30-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_ap_3" to TranslatedTimelineFields(
            dateLabel = "माह 2 फ़ॉलो-अप • दिन 64",
            headline = "विस्तृत फ़ोरेंसिक चार्जशीट दाखिल; अभिभावकों ने विधायक और सांसद से स्थायी स्कूल सुरक्षा मार्शल की मांग की",
            details = "चेनसॉ की फ़ोरेंसिक रिपोर्ट, डिजिटल स्टॉकिंग रिकॉर्ड और 18 चश्मदीद छात्रों के बयान विशेष POCSO कोर्ट में दाखिल किए गए। अभिभावक संघ ने स्थानीय विधायक सुंदरपु विजय कुमार और सांसद सी. एम. रमेश को ज्ञापन सौंपा।",
            sourceName = "सुरक्षा 60-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_mh_1" to TranslatedTimelineFields(
            dateLabel = "दिन 1–5 • जन आक्रोश और SIT का गठन",
            headline = "FIR में 11 घंटे की देरी पर 3 वरिष्ठ पुलिसकर्मी निलंबित; विशेष जांच दल (SIT) नियुक्त",
            details = "बदलापुर रेलवे स्टेशन पर भारी नागरिक विरोध के बाद महाराष्ट्र सरकार ने थाना प्रभारी को निलंबित किया और विशेष SIT का गठन किया।",
            sourceName = "SIT आधिकारिक विज्ञप्ति"
        ),
        "upd_mh_2" to TranslatedTimelineFields(
            dateLabel = "माह 2 फ़ॉलो-अप • उच्च न्यायालय की निगरानी",
            headline = "स्कूल ट्रस्टियों की गिरफ्तारी में देरी पर बॉम्बे हाई कोर्ट ने पुलिस को लगाई फटकार",
            details = "उच्च न्यायालय ने सवाल उठाया कि अपराध छिपाने वाले स्कूल प्रबंधन अधिकारियों को POCSO धारा 21 के तहत तुरंत गिरफ्तार क्यों नहीं किया गया।",
            sourceName = "उच्च न्यायालय विधि संवाददाता"
        ),
        "upd_mh_3" to TranslatedTimelineFields(
            dateLabel = "माह 3 फ़ॉलो-अप • स्कूल सुरक्षा ऑडिट",
            headline = "स्कूल ट्रस्टियों के खिलाफ ट्रायल जारी; ठाणे के 38% स्कूलों में अब भी महिला वॉशरूम अटेंडेंट नहीं",
            details = "बदलापुर और ठाणे में आरटीआई और नागरिक ऑडिट से पता चला कि सरकारी परिपत्रों के बावजूद एक-तिहाई से अधिक निजी स्कूलों ने सहायक कर्मचारियों का पुलिस सत्यापन पूरा नहीं किया था।",
            sourceName = "सुरक्षा 90-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_ka_1" to TranslatedTimelineFields(
            dateLabel = "दिन 1 • परिसर में गिरफ़्तारी और राज्यव्यापी छात्र प्रदर्शन",
            headline = "एक घंटे के भीतर हमलावर गिरफ्तार; कर्नाटक भर के छात्रों ने अनिवार्य बायोमेट्रिक कैंपस गेट की मांग की",
            details = "विद्यानगर पुलिस ने आरोपी को गिरफ्तार किया और फास्ट-ट्रैक अभियोजन के लिए जांच CID को सौंप दी।",
            sourceName = "कर्नाटक CID"
        ),
        "upd_ka_2" to TranslatedTimelineFields(
            dateLabel = "माह 3 फ़ॉलो-अप • 483 पन्नों की CID चार्जशीट",
            headline = "CID ने 92 गवाहों के साथ 483 पन्नों की चार्जशीट दाखिल की; विशेष फास्ट-ट्रैक कोर्ट नामित",
            details = "अपराध जांच विभाग (CID) ने निर्धारित समय सीमा के भीतर फ़ोरेंसिक, डिजिटल और चश्मदीद साक्ष्य प्रस्तुत किए।",
            sourceName = "सुरक्षा 90-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_ka_3" to TranslatedTimelineFields(
            dateLabel = "माह 4 फ़ॉलो-अप • गवाहों की परीक्षा",
            headline = "प्रतिदिन गवाहों की सुनवाई जारी; परिवार ने स्टॉकिंग के खिलाफ सख्त गैर-जमानती कानून की मांग की",
            details = "पीड़िता के माता-पिता ने राज्य और संसदीय प्रतिनिधियों से मुलाकात कर छात्राओं द्वारा पहली बार की गई स्टॉकिंग शिकायत पर तत्काल गैर-जमानती कार्रवाई की मांग की।",
            sourceName = "सुरक्षा 120-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_wb_1" to TranslatedTimelineFields(
            dateLabel = "सप्ताह 1 • कलकत्ता हाई कोर्ट ने जांच CBI को सौंपी",
            headline = "उच्च न्यायालय ने केस डायरी और फ़ोरेंसिक नमूने तुरंत CBI को सौंपने का आदेश दिया",
            details = "अस्पताल प्रशासन द्वारा त्वरित संस्थागत FIR के बजाय अस्वाभाविक मृत्यु रिपोर्ट दर्ज करने की गंभीर चूक का संज्ञान लेते हुए अदालत ने जांच स्थानांतरित कर दी।",
            sourceName = "कलकत्ता उच्च न्यायालय आदेश"
        ),
        "upd_wb_2" to TranslatedTimelineFields(
            dateLabel = "माह 2 फ़ॉलो-अप • CBI चार्जशीट और साक्ष्य से छेड़छाड़ पर गिरफ़्तारी",
            headline = "CBI ने मुख्य चार्जशीट दाखिल की; पूर्व अस्पताल प्राचार्य और स्थानीय थाना प्रभारी साक्ष्य मिटाने के आरोप में गिरफ्तार",
            details = "जांच का दायरा संस्थागत लीपापोती और अपराध स्थल को सील करने में हुई देरी तक बढ़ाया गया।",
            sourceName = "सुरक्षा 60-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_wb_3" to TranslatedTimelineFields(
            dateLabel = "माह 3 फ़ॉलो-अप • सियालदह कोर्ट में इन-कैमरा ट्रायल",
            headline = "सियालदह कोर्ट में प्रतिदिन इन-कैमरा सुनवाई शुरू; जूनियर डॉक्टरों ने अस्पताल सीसीटीवी और ड्यूटी रूम के वादों का ऑडिट किया",
            details = "घटना के तीन महीने बाद नागरिक पर्यवेक्षकों ने जांच की कि क्या वादे के अनुसार पैनिक बटन और अलग महिला विश्राम कक्ष वास्तव में पूरे हुए हैं।",
            sourceName = "सुरक्षा 90-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_up_1" to TranslatedTimelineFields(
            dateLabel = "दिन 1 • प्रारंभ में सामूहिक दुष्कर्म की धारा के बिना FIR दर्ज",
            headline = "छात्रों ने सामूहिक दुष्कर्म की धाराएं जोड़ने और परिसर की चारदीवारी बनाने की मांग को लेकर कैंपस गेट बंद किए",
            details = "धारा 164 CrPC के तहत मजिस्ट्रेट के समक्ष पीड़िता के बयान के बाद FIR में सामूहिक दुष्कर्म की धाराएं जोड़ी गईं।",
            sourceName = "लंका थाना रिकॉर्ड"
        ),
        "upd_up_2" to TranslatedTimelineFields(
            dateLabel = "माह 2 फ़ॉलो-अप • 60वें दिन गिरफ़्तारी",
            headline = "60 दिनों की चुप्पी के बाद पुलिस ने तीनों आरोपियों को गिरफ्तार किया और अपराध में प्रयुक्त मोटरसाइकिल जब्त की",
            details = "छात्र संगठनों के निरंतर दबाव ने गिरफ्तारी होने तक मामले को जीवित रखा।",
            sourceName = "सुरक्षा 60-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_up_3" to TranslatedTimelineFields(
            dateLabel = "माह 6 फ़ॉलो-अप • जमानत और गवाह सुरक्षा निगरानी",
            headline = "उच्च न्यायालय ने दो आरोपियों को सशर्त जमानत दी; अधिवक्ताओं ने पीड़िता के लिए सख्त गवाह सुरक्षा की मांग की",
            details = "यह प्रमाणित करता है कि गिरफ्तारी के बाद मामलों को भुलाया नहीं जा सकता: ट्रायल में जिरह और पीड़िता की सुरक्षा के लिए निरंतर सार्वजनिक सतर्कता आवश्यक है।",
            sourceName = "सुरक्षा 180-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_dl_1" to TranslatedTimelineFields(
            dateLabel = "दिन 2 • बुलंदशहर से आरोपी गिरफ़्तार",
            headline = "तकनीकी सर्विलांस के जरिए आरोपी को यूपी से पकड़कर रिमांड पर लिया गया; हथियार बरामद",
            details = "दिल्ली पुलिस ने अपराध में प्रयुक्त चाकू और कपड़े बरामद किए।",
            sourceName = "दिल्ली पुलिस क्राइम ब्रांच"
        ),
        "upd_dl_2" to TranslatedTimelineFields(
            dateLabel = "माह 1 फ़ॉलो-अप • 640 पन्नों की चार्जशीट",
            headline = "रोहिणी कोर्ट में FSL डीएनए और सीसीटीवी साक्ष्यों के साथ 640 पन्नों की चार्जशीट दाखिल",
            details = "पूर्व नियोजित स्टॉकिंग साबित करने के लिए फ़ोरेंसिक और डिजिटल चैट रिकॉर्ड शामिल किए गए।",
            sourceName = "सुरक्षा 30-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_dl_3" to TranslatedTimelineFields(
            dateLabel = "माह 5 फ़ॉलो-अप • पीड़ित परिवार मुआवजा और ट्रायल",
            headline = "प्रमुख अभियोजन गवाहों की गवाही पूरी; कानूनी सहायता प्रकोष्ठ द्वारा अंतरिम मुआवजे के वितरण की निगरानी",
            details = "यह सुनिश्चित किया जा रहा है कि नाबालिग पीड़िता के छोटे भाई-बहनों को शिक्षा और आवास सहायता समय पर मिले।",
            sourceName = "सुरक्षा 150-दिवसीय फ़ॉलो-अप डेस्क"
        ),
        "upd_rj_1" to TranslatedTimelineFields(
            dateLabel = "दिन 4 • विशेष पुलिस टीम ने छात्रा को सुरक्षित बचाया और 3 को गिरफ़्तार किया",
            headline = "अंतर-राज्यीय पुलिस टीम ने संदिग्धों को पकड़ा; गैर-पंजीकृत पीजी हॉस्टल सील",
            details = "कोटा पुलिस ने नाबालिग छात्रा को सुरक्षित बचाया और बिना महिला वार्डन के चल रहे दो हॉस्टलों को सील कर दिया।",
            sourceName = "कोटा शहर पुलिस"
        ),
        "upd_rj_2" to TranslatedTimelineFields(
            dateLabel = "माह 1 फ़ॉलो-अप • डिजिटल फ़ोरेंसिक और चार्जशीट",
            headline = "चार्जशीट दाखिल; सीसीटीवी और महिला सुरक्षा गार्ड न होने पर 42 कोचिंग हॉस्टलों को कारण बताओ नोटिस",
            details = "फ़ॉलो-अप ऑडिट में यह ट्रैक किया जा रहा है कि 30 दिन की समय सीमा के बाद कितने हॉस्टलों ने वास्तव में नियमों का पालन किया।",
            sourceName = "सुरक्षा 30-दिवसीय फ़ॉलो-अप डेस्क"
        )
    )

    val hindiHelplineMap: Map<String, TranslatedHelplineFields> = mapOf(
        "help_112" to TranslatedHelplineFields(
            title = "राष्ट्रीय आपातकालीन प्रतिक्रिया प्रणाली (ERSS)",
            subtitle = "सभी भारतीय राज्यों में जीपीएस लोकेशन ट्रैकिंग के साथ तत्काल पुलिस, एम्बुलेंस और पीसीआर वैन सहायता।",
            availability = "24x7 • अखिल भारतीय टोल-फ्री",
            targetGroup = "सभी बालिकाएं, किशोरियां और महिलाएं"
        ),
        "help_1098" to TranslatedHelplineFields(
            title = "चाइल्डलाइन इंडिया (स्कूली छात्राएं एवं 18 वर्ष से कम नाबालिग)",
            subtitle = "18 वर्ष से कम आयु की बालिकाओं के लिए आपातकालीन बचाव, POCSO हस्तक्षेप, स्कूल सुरक्षा शिकायत और आश्रय।",
            availability = "24x7 • महिला एवं बाल विकास मंत्रालय",
            targetGroup = "नाबालिग बालिकाएं और स्कूली छात्राएं"
        ),
        "help_1091" to TranslatedHelplineFields(
            title = "महिला पुलिस आपातकालीन हेल्पलाइन",
            subtitle = "जिला महिला पुलिस सेल, निर्भया/दिशा स्क्वाड और स्टॉकिंग/हमला प्रतिक्रिया इकाइयों से सीधा संपर्क।",
            availability = "24x7 • राज्य पुलिस नियंत्रण कक्ष",
            targetGroup = "महिलाएं और कॉलेज छात्राएं"
        ),
        "help_181" to TranslatedHelplineFields(
            title = "अभयम / वन-स्टॉप महिला हेल्पलाइन",
            subtitle = "चिकित्सा सहायता, कानूनी परामर्श, FIR सहायता और अस्थायी आश्रय के लिए वन-स्टॉप सखी केंद्रों से जोड़ती है।",
            availability = "24x7 • राष्ट्रीय टोल-फ्री",
            targetGroup = "घरेलू हिंसा, दहेज एवं उत्पीड़न पीड़िताएं"
        ),
        "help_1930" to TranslatedHelplineFields(
            title = "राष्ट्रीय साइबर अपराध हेल्पलाइन",
            subtitle = "बिना अनुमति फोटो, डीपफेक मॉर्फिंग, साइबर स्टॉकिंग और ब्लैकमेल सामग्री को तुरंत हटाने और FIR दर्ज कराने में सहायता।",
            availability = "24x7 • भारतीय साइबर अपराध समन्वय केंद्र",
            targetGroup = "साइबर उत्पीड़न का सामना कर रहीं किशोरियां और महिलाएं"
        ),
        "help_ncw" to TranslatedHelplineFields(
            title = "राष्ट्रीय महिला आयोग (NCW) हेल्पलाइन",
            subtitle = "जब स्थानीय पुलिस FIR दर्ज करने में देरी करे या मना करे, तो 24x7 आपातकालीन शिकायत और संज्ञान पोर्टल।",
            availability = "24x7 • वैधानिक राष्ट्रीय आयोग",
            targetGroup = "पुलिस की उदासीनता एवं संस्थागत शिकायतें"
        )
    )

    val hindiLegalGuideMap: Map<String, TranslatedLegalGuideFields> = mapOf(
        "law_zero_fir" to TranslatedLegalGuideFields(
            codeBadge = "BNSS धारा 173 • ज़ीरो FIR (Zero FIR)",
            title = "भारत के किसी भी पुलिस थाने में ज़ीरो FIR दर्ज कराने का अधिकार",
            summary = "कोई भी पुलिस अधिकारी यह कहकर किसी लड़की या महिला के खिलाफ अपराध की रिपोर्ट दर्ज करने से मना नहीं कर सकता कि घटना उनके थाना क्षेत्र के बाहर हुई है।",
            actionChecklist = listOf(
                "अपराध चाहे किसी भी शहर या जिले में हुआ हो, निकटतम पुलिस थाने में ज़ीरो FIR दर्ज करने की मांग करें।",
                "यौन उत्पीड़न, स्टॉकिंग या एसिड/हथियार हमले के मामलों में पीड़िता का बयान अनिवार्य रूप से महिला पुलिस अधिकारी द्वारा ही दर्ज किया जाना चाहिए।",
                "यदि कोई पुलिस अधिकारी महिला के विरुद्ध संज्ञेय अपराध दर्ज करने से इनकार करता है, तो उसे BNS की धारा 199 के तहत 6 महीने से 2 साल तक के कठोर कारावास की सजा हो सकती है।",
                "FIR दर्ज होते ही उसकी एक निःशुल्क प्रति प्राप्त करना आपका कानूनी अधिकार है।"
            ),
            statutoryCitation = "भारतीय नागरिक सुरक्षा संहिता (BNSS), 2023 — धारा 173(1) एवं BNS धारा 199"
        ),
        "law_pocso_school" to TranslatedLegalGuideFields(
            codeBadge = "POCSO अधिनियम • नाबालिग और स्कूल सुरक्षा",
            title = "18 वर्ष से कम आयु की छात्राओं और किशोरियों का संरक्षण (POCSO अधिनियम)",
            summary = "स्कूल, हॉस्टल या ट्यूशन सेंटर में 18 वर्ष से कम आयु की लड़की के खिलाफ अपराध, स्टॉकिंग या सुरक्षा चूक होने पर सख्त कानूनी प्रावधान।",
            actionChecklist = listOf(
                "अनिवार्य रिपोर्टिंग (धारा 19 व 21): कोई भी स्कूल प्रिंसिपल, प्रबंधन ट्रस्टी या कर्मचारी जो अपराध की सूचना देने में विफल रहता है या उसे दबाने का प्रयास करता है, वह दंडनीय अपराध करता है (6 महीने से 1 वर्ष तक का कारावास)।",
                "बाल-अनुकूल बयान: नाबालिग बच्ची का बयान उसके घर या उसकी पसंद के स्थान पर सादे कपड़ों में महिला अधिकारी द्वारा दर्ज किया जाना चाहिए—उसे कभी भी पुलिस थाने में रोककर नहीं रखा जा सकता।",
                "पहचान की गोपनीयता (धारा 23): मीडिया या सोशल नेटवर्क पर नाबालिग पीड़िता का नाम, स्कूल, फोटो या पारिवारिक विवरण उजागर करना कानूनन दंडनीय अपराध है।",
                "विशेष POCSO न्यायालय: मुकदमे की सुनवाई बाल-अनुकूल विशेष न्यायालय में संज्ञान लेने के 1 वर्ष के भीतर पूरी की जानी चाहिए।"
            ),
            statutoryCitation = "लैंगिक अपराधों से बालकों का संरक्षण (POCSO) अधिनियम, 2012 — धारा 11, 19, 21, 23, 24 एवं 35"
        ),
        "law_stalking_weapons" to TranslatedLegalGuideFields(
            codeBadge = "BNS धारा 78, 109 एवं 124",
            title = "लड़कियों और महिलाओं का पीछा करना (Stalking), हथियार से हमला और एसिड अटैक",
            summary = "शारीरिक या डिजिटल रूप से पीछा करने, कक्षा/कैंपस में जबरन घुसने और हथियार या ज्वलनशील पदार्थ से हमला करने पर कठोर दंड का प्रावधान है।",
            actionChecklist = listOf(
                "सभी डिजिटल साक्ष्य (कॉल लॉग, व्हाट्सएप संदेश, इंस्टाग्राम धमकियां और चश्मदीदों के नंबर) बिना डिलीट किए सुरक्षित रखें।",
                "BNS धारा 78 के तहत पहली बार स्टॉकिंग का दोषी पाए जाने पर 3 साल तक की जेल और दोबारा अपराध करने पर 5 साल तक की सजा का प्रावधान है।",
                "जान से मारने की नीयत से हथियार, चाकू या चेनसॉ से किए गए हमलों पर BNS धारा 109 (हत्या का प्रयास — आजीवन कारावास तक) के तहत मुकदमा चलाया जाता है।",
                "सभी सरकारी और निजी अस्पताल BNS धारा 200 के तहत पुलिस के आने का इंतजार किए बिना पीड़िता को तुरंत 100% निःशुल्क प्राथमिक उपचार और इलाज देने के लिए कानूनी रूप से बाध्य हैं।"
            ),
            statutoryCitation = "भारतीय न्याय संहिता (BNS), 2023 — धारा 78, 109, 124 एवं 200"
        ),
        "law_mp_mla_accountability" to TranslatedLegalGuideFields(
            codeBadge = "नागरिक जवाबदेही • सांसद (MP) एवं विधायक (MLA)",
            title = "लंबित मामलों के लिए अपने क्षेत्र के विधायक (MLA) और सांसद (MP) की जवाबदेही तय करना",
            summary = "नागरिक और अभिभावक रुकी हुई जांच और कैंपस सुरक्षा चूकों को निर्वाचित जनप्रतिनिधियों के समक्ष औपचारिक रूप से कैसे उठा सकते हैं।",
            actionChecklist = listOf(
                "जिला सतर्कता एवं निगरानी: प्रत्येक सांसद और विधायक अपने निर्वाचन क्षेत्र में महिलाओं के खिलाफ लंबित अपराधों के संबंध में संसद या विधानसभा में तारांकित/अतारांकित प्रश्न उठा सकते हैं।",
                "निर्भया फंड और सांसद/विधायक निधि: नागरिक स्ट्रीट लाइटिंग, स्कूल सीसीटीवी गेट और महिला पुलिस गश्त बूथों के लिए स्थानीय क्षेत्र विकास निधि के आवंटन की मांग कर सकते हैं।",
                "FIR संख्या और 'घटना के बाद बीते दिनों' का उल्लेख करते हुए जिला कलेक्टर/एसपी तथा क्षेत्र के विधायक/सांसद कार्यालय को दिनांकित ज्ञापन सौंपें।",
                "यह ट्रैक करने के लिए सुरक्षा क्रॉनिकल के केस डॉसियर का उपयोग करें कि क्या जनप्रतिनिधि ने 30, 60 और 90 दिनों के बाद कोई आधिकारिक फ़ॉलो-अप जारी किया।"
            ),
            statutoryCitation = "संसदीय एवं राज्य विधायी निगरानी मानदंड + जिला स्तरीय अपराध समीक्षा प्रोटोकॉल"
        )
    )

    fun buildSeedCacheEntities(): List<TranslationCacheEntity> {
        val now = System.currentTimeMillis()
        val entities = mutableListOf<TranslationCacheEntity>()
        for ((caseId, fields) in hindiCaseMap) {
            entities.add(TranslationCacheEntity(caseId, "hi", "title", fields.title, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "summary", fields.summary, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "fullReport", fields.fullReport, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "policeJurisdiction", fields.policeJurisdiction, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "victimCategory", fields.victimCategory, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "crimeCategory", fields.crimeCategory, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "incidentDateLabel", fields.incidentDateLabel, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "triggerWarning", fields.triggerWarning, now))
            entities.add(TranslationCacheEntity(caseId, "hi", "relevantLaws", fields.relevantLaws.joinToString("||"), now))
            entities.add(TranslationCacheEntity(caseId, "hi", "sourceName", fields.sourceName, now))
        }
        for ((updId, fields) in hindiTimelineMap) {
            entities.add(TranslationCacheEntity(updId, "hi", "dateLabel", fields.dateLabel, now))
            entities.add(TranslationCacheEntity(updId, "hi", "headline", fields.headline, now))
            entities.add(TranslationCacheEntity(updId, "hi", "details", fields.details, now))
            entities.add(TranslationCacheEntity(updId, "hi", "sourceName", fields.sourceName, now))
        }
        for ((hlId, fields) in hindiHelplineMap) {
            entities.add(TranslationCacheEntity(hlId, "hi", "title", fields.title, now))
            entities.add(TranslationCacheEntity(hlId, "hi", "subtitle", fields.subtitle, now))
            entities.add(TranslationCacheEntity(hlId, "hi", "availability", fields.availability, now))
            entities.add(TranslationCacheEntity(hlId, "hi", "targetGroup", fields.targetGroup, now))
        }
        for ((lawId, fields) in hindiLegalGuideMap) {
            entities.add(TranslationCacheEntity(lawId, "hi", "codeBadge", fields.codeBadge, now))
            entities.add(TranslationCacheEntity(lawId, "hi", "title", fields.title, now))
            entities.add(TranslationCacheEntity(lawId, "hi", "summary", fields.summary, now))
            entities.add(TranslationCacheEntity(lawId, "hi", "actionChecklist", fields.actionChecklist.joinToString("||"), now))
            entities.add(TranslationCacheEntity(lawId, "hi", "statutoryCitation", fields.statutoryCitation, now))
        }
        return entities
    }

    fun applyHindiToCaseDossier(
        dossier: CaseDossier,
        cacheLookup: Map<String, Map<String, String>>
    ): CaseDossier {
        val seedFields = hindiCaseMap[dossier.id]
        val cached = cacheLookup[dossier.id]

        val translatedUpdates = dossier.timelineUpdates.map { upd ->
            applyHindiToTimelineUpdate(upd, cacheLookup)
        }

        if (seedFields != null) {
            return dossier.copy(
                title = seedFields.title,
                summary = seedFields.summary,
                fullReport = seedFields.fullReport,
                policeJurisdiction = seedFields.policeJurisdiction,
                victimCategory = seedFields.victimCategory,
                crimeCategory = seedFields.crimeCategory,
                incidentDateLabel = seedFields.incidentDateLabel,
                triggerWarning = seedFields.triggerWarning,
                relevantLaws = seedFields.relevantLaws,
                sourceName = seedFields.sourceName,
                timelineUpdates = translatedUpdates,
                isMachineTranslated = false,
                originalTitle = dossier.originalTitle,
                originalSummary = dossier.originalSummary,
                originalFullReport = dossier.originalFullReport
            )
        }

        if (cached != null) {
            val tTitle = cached["title"] ?: dossier.title
            val tSummary = cached["summary"] ?: dossier.summary
            val tReport = cached["fullReport"] ?: dossier.fullReport
            val isTranslated = tTitle != dossier.originalTitle || tSummary != dossier.originalSummary
            return dossier.copy(
                title = tTitle,
                summary = tSummary,
                fullReport = tReport,
                timelineUpdates = translatedUpdates,
                isMachineTranslated = isTranslated,
                originalTitle = dossier.originalTitle,
                originalSummary = dossier.originalSummary,
                originalFullReport = dossier.originalFullReport
            )
        }

        return dossier.copy(timelineUpdates = translatedUpdates)
    }

    fun applyHindiToTimelineUpdate(
        update: TimelineUpdateItem,
        cacheLookup: Map<String, Map<String, String>>
    ): TimelineUpdateItem {
        val seed = hindiTimelineMap[update.id]
        if (seed != null) {
            return update.copy(
                dateLabel = seed.dateLabel,
                headline = seed.headline,
                details = seed.details,
                sourceName = seed.sourceName,
                isMachineTranslated = false,
                originalHeadline = update.originalHeadline,
                originalDetails = update.originalDetails
            )
        }
        val cached = cacheLookup[update.id]
        if (cached != null) {
            val tHead = cached["headline"] ?: update.headline
            val tDet = cached["details"] ?: update.details
            return update.copy(
                headline = tHead,
                details = tDet,
                isMachineTranslated = tHead != update.originalHeadline || tDet != update.originalDetails,
                originalHeadline = update.originalHeadline,
                originalDetails = update.originalDetails
            )
        }
        return update
    }

    fun getLocalizedHelplines(langCode: String, cacheLookup: Map<String, Map<String, String>>): List<EmergencyHelpline> {
        val base = SurakshaArchiveSeedData.emergencyHelplines
        if (langCode == "en") return base
        return base.map { item ->
            if (langCode == "hi") {
                val hi = hindiHelplineMap[item.id]
                if (hi != null) {
                    return@map item.copy(
                        title = hi.title,
                        subtitle = hi.subtitle,
                        availability = hi.availability,
                        targetGroup = hi.targetGroup
                    )
                }
            }
            val cached = cacheLookup[item.id]
            if (cached != null) {
                item.copy(
                    title = cached["title"] ?: item.title,
                    subtitle = cached["subtitle"] ?: item.subtitle,
                    availability = cached["availability"] ?: item.availability,
                    targetGroup = cached["targetGroup"] ?: item.targetGroup
                )
            } else {
                item
            }
        }
    }

    fun getLocalizedLegalGuides(langCode: String, cacheLookup: Map<String, Map<String, String>>): List<LegalRightGuide> {
        val base = SurakshaArchiveSeedData.legalRightsGuides
        if (langCode == "en") return base
        return base.map { item ->
            if (langCode == "hi") {
                val hi = hindiLegalGuideMap[item.id]
                if (hi != null) {
                    return@map item.copy(
                        codeBadge = hi.codeBadge,
                        title = hi.title,
                        summary = hi.summary,
                        actionChecklist = hi.actionChecklist,
                        statutoryCitation = hi.statutoryCitation
                    )
                }
            }
            val cached = cacheLookup[item.id]
            if (cached != null) {
                item.copy(
                    codeBadge = cached["codeBadge"] ?: item.codeBadge,
                    title = cached["title"] ?: item.title,
                    summary = cached["summary"] ?: item.summary,
                    actionChecklist = cached["actionChecklist"]?.split("||")?.filter { it.isNotBlank() } ?: item.actionChecklist,
                    statutoryCitation = cached["statutoryCitation"] ?: item.statutoryCitation
                )
            } else {
                item
            }
        }
    }
}
