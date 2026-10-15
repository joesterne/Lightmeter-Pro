package com.example.data.local

import com.example.data.model.CameraModel
import com.example.data.model.LensModel
import com.example.data.model.SensorFormat

object DefaultData {

    val BRANDS = listOf(
        "Canon",
        "Sony",
        "Nikon",
        "Leica",
        "Fujifilm",
        "Hasselblad",
        "Panasonic"
    )

    val CAMERAS = listOf(
        // Canon
        CameraModel("Canon", "EOS R5", SensorFormat.FULL_FRAME, 100, 51200, "1/8000", true),
        CameraModel("Canon", "EOS R6 Mark II", SensorFormat.FULL_FRAME, 100, 102400, "1/8000", true),
        CameraModel("Canon", "EOS R3", SensorFormat.FULL_FRAME, 100, 102400, "1/64000", true),
        CameraModel("Canon", "EOS 5D Mark IV", SensorFormat.FULL_FRAME, 100, 32000, "1/8000", false),
        CameraModel("Canon", "EOS R7", SensorFormat.APS_C_CANON, 100, 32000, "1/8000", true),

        // Sony
        CameraModel("Sony", "Alpha A7 IV", SensorFormat.FULL_FRAME, 100, 51200, "1/8000", true),
        CameraModel("Sony", "Alpha A7R V", SensorFormat.FULL_FRAME, 100, 32000, "1/8000", true),
        CameraModel("Sony", "Alpha A1", SensorFormat.FULL_FRAME, 100, 32000, "1/32000", true),
        CameraModel("Sony", "FX3 Cinema", SensorFormat.FULL_FRAME, 80, 409600, "1/8000", true),
        CameraModel("Sony", "Alpha A6700", SensorFormat.APS_C_SONY_NIKON, 100, 32000, "1/8000", true),

        // Nikon
        CameraModel("Nikon", "Z8", SensorFormat.FULL_FRAME, 64, 25600, "1/32000", true),
        CameraModel("Nikon", "Z9", SensorFormat.FULL_FRAME, 64, 25600, "1/32000", true),
        CameraModel("Nikon", "Z6 III", SensorFormat.FULL_FRAME, 100, 64000, "1/8000", true),
        CameraModel("Nikon", "Zf Retro", SensorFormat.FULL_FRAME, 100, 64000, "1/8000", true),
        CameraModel("Nikon", "D850 DSLR", SensorFormat.FULL_FRAME, 64, 25600, "1/8000", false),

        // Leica
        CameraModel("Leica", "M11 Rangefinder", SensorFormat.FULL_FRAME, 64, 50000, "1/16000", false),
        CameraModel("Leica", "Q3 28mm", SensorFormat.FULL_FRAME, 50, 100000, "1/16000", true),
        CameraModel("Leica", "SL2-S", SensorFormat.FULL_FRAME, 50, 100000, "1/16000", true),
        CameraModel("Leica", "M10-R", SensorFormat.FULL_FRAME, 100, 50000, "1/4000", false),

        // Fujifilm
        CameraModel("Fujifilm", "X-T5", SensorFormat.APS_C_SONY_NIKON, 125, 12800, "1/8000", true),
        CameraModel("Fujifilm", "X-H2S", SensorFormat.APS_C_SONY_NIKON, 160, 12800, "1/8000", true),
        CameraModel("Fujifilm", "GFX 100 II", SensorFormat.MEDIUM_FORMAT, 80, 12800, "1/4000", true),

        // Hasselblad
        CameraModel("Hasselblad", "X2D 100C", SensorFormat.MEDIUM_FORMAT, 64, 25600, "1/4000", true),
        CameraModel("Hasselblad", "907X 50C", SensorFormat.MEDIUM_FORMAT, 100, 25600, "1/2000", false),

        // Panasonic Lumix
        CameraModel("Panasonic", "Lumix S5 II", SensorFormat.FULL_FRAME, 100, 51200, "1/8000", true),
        CameraModel("Panasonic", "Lumix GH6", SensorFormat.MICRO_FOUR_THIRDS, 100, 25600, "1/8000", true)
    )

    val LENSES = listOf(
        // Canon
        LensModel("Canon", "RF 24-70mm f/2.8L IS USM", 24, 70, 2.8f, 22f, true),
        LensModel("Canon", "RF 50mm f/1.2L USM", 50, 50, 1.2f, 16f, false),
        LensModel("Canon", "RF 70-200mm f/2.8L IS USM", 70, 200, 2.8f, 22f, true),
        LensModel("Canon", "RF 85mm f/1.2L USM DS", 85, 85, 1.2f, 16f, false),
        LensModel("Canon", "RF 15-35mm f/2.8L IS USM", 15, 35, 2.8f, 22f, true),
        LensModel("Canon", "EF 50mm f/1.8 STM", 50, 50, 1.8f, 22f, false),

        // Sony
        LensModel("Sony", "FE 24-70mm f/2.8 GM II", 24, 70, 2.8f, 22f, false),
        LensModel("Sony", "FE 50mm f/1.2 GM", 50, 50, 1.2f, 16f, false),
        LensModel("Sony", "FE 85mm f/1.4 GM II", 85, 85, 1.4f, 16f, false),
        LensModel("Sony", "FE 70-200mm f/2.8 GM OSS II", 70, 200, 2.8f, 22f, true),
        LensModel("Sony", "FE 16-35mm f/2.8 GM II", 16, 35, 2.8f, 22f, false),
        LensModel("Sony", "FE 35mm f/1.4 GM", 35, 35, 1.4f, 16f, false),

        // Nikon
        LensModel("Nikon", "NIKKOR Z 24-70mm f/2.8 S", 24, 70, 2.8f, 22f, false),
        LensModel("Nikon", "NIKKOR Z 50mm f/1.2 S", 50, 50, 1.2f, 16f, false),
        LensModel("Nikon", "NIKKOR Z 85mm f/1.2 S", 85, 85, 1.2f, 16f, false),
        LensModel("Nikon", "NIKKOR Z 70-200mm f/2.8 VR S", 70, 200, 2.8f, 22f, true),
        LensModel("Nikon", "NIKKOR Z 14-24mm f/2.8 S", 14, 24, 2.8f, 22f, false),

        // Leica
        LensModel("Leica", "Summilux-M 35mm f/1.4 ASPH", 35, 35, 1.4f, 16f, false),
        LensModel("Leica", "Noctilux-M 50mm f/0.95 ASPH", 50, 50, 0.95f, 16f, false),
        LensModel("Leica", "Summicron-M 50mm f/2", 50, 50, 2.0f, 16f, false),
        LensModel("Leica", "APO-Summicron-SL 75mm f/2 ASPH", 75, 75, 2.0f, 22f, false),
        LensModel("Leica", "Vario-Elmarit-SL 24-70mm f/2.8 ASPH", 24, 70, 2.8f, 22f, false),

        // Fujifilm
        LensModel("Fujifilm", "XF 16-55mm f/2.8 R LM WR", 16, 55, 2.8f, 22f, false),
        LensModel("Fujifilm", "XF 56mm f/1.2 R WR", 56, 56, 1.2f, 16f, false),
        LensModel("Fujifilm", "XF 33mm f/1.4 R LM WR", 33, 33, 1.4f, 16f, false),
        LensModel("Fujifilm", "GF 110mm f/2 R LM WR (Medium Format)", 110, 110, 2.0f, 22f, false),

        // Hasselblad
        LensModel("Hasselblad", "XCD 55mm f/2.5 V", 55, 55, 2.5f, 32f, false),
        LensModel("Hasselblad", "XCD 90mm f/2.5 V", 90, 90, 2.5f, 32f, false),

        // Panasonic
        LensModel("Panasonic", "Lumix S PRO 24-70mm f/2.8", 24, 70, 2.8f, 22f, false),
        LensModel("Panasonic", "Lumix S PRO 50mm f/1.4", 50, 50, 1.4f, 16f, false)
    )

    val INITIAL_PRESETS = listOf(
        PresetEntity(
            name = "Golden Hour Portrait",
            category = "Portrait",
            environmentDescription = "Warm backlighting 30-45 minutes before sunset, soft diffused shadows.",
            targetEv100 = 11.0f,
            iso = 100,
            shutterSpeed = "1/500",
            aperture = 1.4f,
            focalLengthMm = 85,
            ndFilterStops = 0,
            lightingRatioNotes = "Sun as rim/hair key (2:1 ratio to shadow fill reflector)",
            isCustom = false
        ),
        PresetEntity(
            name = "Sunny 16 Street",
            category = "Street",
            environmentDescription = "Direct mid-day sunlight, sharp distinct shadows, classic street photography.",
            targetEv100 = 15.0f,
            iso = 200,
            shutterSpeed = "1/250",
            aperture = 16f,
            focalLengthMm = 35,
            ndFilterStops = 0,
            lightingRatioNotes = "High contrast 4:1 ambient sunlight",
            isCustom = false
        ),
        PresetEntity(
            name = "Astrophotography Dark Sky",
            category = "Astrophotography",
            environmentDescription = "Milky Way core, Bortle Class 2-3 dark site, no moon, pinpoint stars.",
            targetEv100 = -6.5f,
            iso = 3200,
            shutterSpeed = "15s",
            aperture = 1.8f,
            focalLengthMm = 20,
            ndFilterStops = 0,
            lightingRatioNotes = "Ambient starlight only. 500 Rule: max 15s to avoid star trailing.",
            isCustom = false
        ),
        PresetEntity(
            name = "Studio 3-Point Fashion",
            category = "Studio",
            environmentDescription = "Controlled strobe lighting with octabox key, strip fill, and hair grid.",
            targetEv100 = 10.0f,
            iso = 100,
            shutterSpeed = "1/160",
            aperture = 8.0f,
            focalLengthMm = 70,
            ndFilterStops = 0,
            lightingRatioNotes = "Key f/8, Fill f/5.6 (2:1 ratio), Hair f/8. Flash sync speed 1/160s.",
            isCustom = false
        ),
        PresetEntity(
            name = "Blue Hour Cityscape",
            category = "Landscape",
            environmentDescription = "20-30 minutes post-sunset, deep cobalt sky balancing street incandescent lamps.",
            targetEv100 = 5.0f,
            iso = 100,
            shutterSpeed = "4s",
            aperture = 11f,
            focalLengthMm = 24,
            ndFilterStops = 0,
            lightingRatioNotes = "Tripod required. Natural light balance with city lights.",
            isCustom = false
        ),
        PresetEntity(
            name = "Indoor Concert / Stage",
            category = "Low Light",
            environmentDescription = "High-energy stage spotlights, moving performers, dark background.",
            targetEv100 = 7.0f,
            iso = 3200,
            shutterSpeed = "1/320",
            aperture = 2.8f,
            focalLengthMm = 70,
            ndFilterStops = 0,
            lightingRatioNotes = "Spot meter on artist faces to prevent highlight blowout.",
            isCustom = false
        )
    )

    val INITIAL_GEAR = listOf(
        GearEntity(
            name = "EOS R5 Mirrorless Body",
            brand = "Canon",
            category = "Camera Body",
            modelNumber = "EOS R5",
            serialNumber = "042021004918",
            condition = "Mint",
            weightGrams = 738,
            inBag = true,
            notes = "Primary studio and landscape workhorse. 45MP."
        ),
        GearEntity(
            name = "RF 24-70mm f/2.8L IS USM",
            brand = "Canon",
            category = "Lens",
            modelNumber = "RF24-70F2.8L",
            serialNumber = "98200142",
            condition = "Excellent",
            weightGrams = 900,
            inBag = true,
            notes = "Daily all-round zoom with 5-stop image stabilization."
        ),
        GearEntity(
            name = "RF 50mm f/1.2L USM",
            brand = "Canon",
            category = "Lens",
            modelNumber = "RF50F1.2L",
            serialNumber = "31900217",
            condition = "Mint",
            weightGrams = 950,
            inBag = true,
            notes = "Ultra-fast portrait prime for low light and creamy bokeh."
        ),
        GearEntity(
            name = "AD600Pro All-in-One Outdoor Flash",
            brand = "Godox",
            category = "Flash / Strobe",
            modelNumber = "AD600Pro",
            serialNumber = "GX-600-4821",
            condition = "Excellent",
            weightGrams = 3000,
            inBag = false,
            notes = "600Ws strobe with TTL, HSS up to 1/8000s, Bowens mount."
        ),
        GearEntity(
            name = "ProMaster Variable ND 77mm",
            brand = "ProMaster",
            category = "Filter",
            modelNumber = "HGX-VND77",
            serialNumber = "VND-77-99",
            condition = "Mint",
            weightGrams = 85,
            inBag = true,
            notes = "1.3 to 8.6 stops ND range (ND2 to ND400)."
        )
    )

    val INITIAL_CAMERA_BODIES = listOf(
        CameraBodyEntity(
            brand = "Canon",
            modelName = "EOS R5",
            sensorFormat = "Full Frame (35mm)",
            cropFactor = 1.0f,
            baseIso = 100,
            maxIso = 51200,
            maxShutterSpeed = "1/8000",
            hasIbis = true,
            serialNumber = "CN-042021004918",
            weightGrams = 738,
            inBag = true,
            notes = "Primary 45MP full-frame studio & landscape body."
        ),
        CameraBodyEntity(
            brand = "Sony",
            modelName = "Alpha A7 IV",
            sensorFormat = "Full Frame (35mm)",
            cropFactor = 1.0f,
            baseIso = 100,
            maxIso = 51200,
            maxShutterSpeed = "1/8000",
            hasIbis = true,
            serialNumber = "SN-55829104",
            weightGrams = 658,
            inBag = true,
            notes = "33MP hybrid mirrorless body with fast Bionic XR autofocus."
        ),
        CameraBodyEntity(
            brand = "Nikon",
            modelName = "Z8",
            sensorFormat = "Full Frame (35mm)",
            cropFactor = 1.0f,
            baseIso = 64,
            maxIso = 25600,
            maxShutterSpeed = "1/32000",
            hasIbis = true,
            serialNumber = "NK-88200194",
            weightGrams = 910,
            inBag = false,
            notes = "Electronic shutter flagship with base ISO 64 dynamic range."
        ),
        CameraBodyEntity(
            brand = "Fujifilm",
            modelName = "X-T5",
            sensorFormat = "APS-C (Sony/Nikon/Fuji)",
            cropFactor = 1.5f,
            baseIso = 125,
            maxIso = 12800,
            maxShutterSpeed = "1/8000",
            hasIbis = true,
            serialNumber = "FJ-91048201",
            weightGrams = 557,
            inBag = false,
            notes = "Compact 40MP street & travel body with classic exposure dials."
        )
    )

    val INITIAL_LENSES = listOf(
        LensEntity(
            brand = "Canon",
            lensName = "RF 24-70mm f/2.8L IS USM",
            minFocalLength = 24,
            maxFocalLength = 70,
            maxAperture = 2.8f,
            minAperture = 22f,
            hasOis = true,
            filterThreadMm = 82,
            serialNumber = "CN-L-98200142",
            weightGrams = 900,
            inBag = true,
            notes = "Standard pro zoom with 5-stop optical image stabilization."
        ),
        LensEntity(
            brand = "Canon",
            lensName = "RF 50mm f/1.2L USM",
            minFocalLength = 50,
            maxFocalLength = 50,
            maxAperture = 1.2f,
            minAperture = 16f,
            hasOis = false,
            filterThreadMm = 77,
            serialNumber = "CN-L-31900217",
            weightGrams = 950,
            inBag = true,
            notes = "Ultra-fast prime for low-light portraits and dreamlike bokeh."
        ),
        LensEntity(
            brand = "Sony",
            lensName = "FE 24-70mm f/2.8 GM II",
            minFocalLength = 24,
            maxFocalLength = 70,
            maxAperture = 2.8f,
            minAperture = 22f,
            hasOis = false,
            filterThreadMm = 82,
            serialNumber = "SN-L-77401928",
            weightGrams = 695,
            inBag = true,
            notes = "Ultra-sharp G Master zoom, extremely lightweight."
        ),
        LensEntity(
            brand = "Sony",
            lensName = "FE 85mm f/1.4 GM II",
            minFocalLength = 85,
            maxFocalLength = 85,
            maxAperture = 1.4f,
            minAperture = 16f,
            hasOis = false,
            filterThreadMm = 77,
            serialNumber = "SN-L-88401211",
            weightGrams = 642,
            inBag = false,
            notes = "Classic focal length for compression and subject separation."
        ),
        LensEntity(
            brand = "Nikon",
            lensName = "NIKKOR Z 24-70mm f/2.8 S",
            minFocalLength = 24,
            maxFocalLength = 70,
            maxAperture = 2.8f,
            minAperture = 22f,
            hasOis = false,
            filterThreadMm = 82,
            serialNumber = "NK-L-44109823",
            weightGrams = 805,
            inBag = false,
            notes = "Constant f/2.8 aperture S-Line professional zoom lens."
        )
    )


    data class GuideTopic(
        val title: String,
        val subtitle: String,
        val summary: String,
        val keyRules: List<String>,
        val tableHeaders: List<String> = emptyList(),
        val tableRows: List<List<String>> = emptyList()
    )

    val OFFLINE_GUIDES = listOf(
        GuideTopic(
            title = "The Sunny 16 Rule",
            subtitle = "Universal outdoor exposure without a light meter",
            summary = "On a clear, bright sunny day, set your aperture to f/16. Your shutter speed will be the reciprocal of your ISO value (e.g. at ISO 100, use 1/100s or 1/125s).",
            keyRules = listOf(
                "Bright Sun on Sand/Snow: f/22 at 1/ISO",
                "Direct Bright Sun (crisp dark shadows): f/16 at 1/ISO",
                "Hazy Sun (soft indistinct shadows): f/11 at 1/ISO",
                "Overcast / Diffused Sky (faint shadows): f/8 at 1/ISO",
                "Heavy Overcast / Open Shade: f/5.6 at 1/ISO",
                "Deep Shade / Sunset: f/4 at 1/ISO"
            ),
            tableHeaders = listOf("Lighting Condition", "Aperture", "Shutter at ISO 100", "EV100"),
            tableRows = listOf(
                listOf("Sand / Snow", "f/22", "1/125s", "16 EV"),
                listOf("Direct Bright Sun", "f/16", "1/125s", "15 EV"),
                listOf("Hazy Sunlight", "f/11", "1/125s", "14 EV"),
                listOf("Cloudy / Diffused", "f/8", "1/125s", "13 EV"),
                listOf("Heavy Overcast", "f/5.6", "1/125s", "12 EV"),
                listOf("Open Shade", "f/4", "1/125s", "11 EV")
            )
        ),
        GuideTopic(
            title = "Reciprocal Rule & Camera Shake",
            subtitle = "Safe handheld shutter speeds for sharp photos",
            summary = "To prevent camera blur from hand tremors, the shutter speed must be at least 1 / (Focal Length × Crop Factor) seconds. Optical Image Stabilization (IS/OIS/VR) or In-Body Image Stabilization (IBIS) allows you to shoot 3 to 5 stops slower.",
            keyRules = listOf(
                "Full Frame Formula: Min Shutter Speed = 1 / Focal Length",
                "APS-C (1.5x): Min Shutter Speed = 1 / (Focal Length × 1.5)",
                "Micro 4/3 (2.0x): Min Shutter Speed = 1 / (Focal Length × 2.0)",
                "With 3-stop IBIS: A 50mm lens (normal 1/50s) can be safely handheld down to 1/6s",
                "Rule of thumb for high-res sensors (45MP+): Double the shutter speed (e.g. 1/160s for 85mm) due to pixel-level magnification."
            ),
            tableHeaders = listOf("Focal Length", "Full Frame Safe", "APS-C Safe", "With 4-Stop IBIS"),
            tableRows = listOf(
                listOf("24mm Wide", "1/30s", "1/40s", "1/2s"),
                listOf("35mm Street", "1/40s", "1/60s", "1/4s"),
                listOf("50mm Standard", "1/60s", "1/80s", "1/4s"),
                listOf("85mm Portrait", "1/100s", "1/160s", "1/8s"),
                listOf("135mm Telephoto", "1/160s", "1/250s", "1/15s"),
                listOf("200mm Long Tele", "1/250s", "1/320s", "1/15s")
            )
        ),
        GuideTopic(
            title = "Flash Sync Speed & High-Speed Sync (HSS)",
            subtitle = "Mastering mechanical focal-plane shutters with strobes",
            summary = "Focal plane shutters have a maximum sync speed (typically 1/200s or 1/250s) where both curtains are fully open simultaneously. Exceeding this speed without HSS causes black banding. HSS pulses the flash rapidly as the slit travels, sacrificing roughly 2 stops of light power.",
            keyRules = listOf(
                "Standard X-Sync: Usually 1/160s to 1/250s depending on camera body.",
                "Leaf Shutters (Hasselblad/Leica Q): Can sync up to 1/2000s or 1/4000s with full flash output!",
                "Guide Number Formula: GN = Aperture (f) × Distance (meters) at ISO 100.",
                "Power Fraction Step: Each stop cut in flash power halves the energy (1/1 -> 1/2 -> 1/4 -> 1/8).",
                "Inverse Square Law: Doubling flash-to-subject distance reduces light to 1/4 (2 stops drop)."
            ),
            tableHeaders = listOf("Flash Power", "Stops Down", "Rel. Output", "Effective GN (Base 60)"),
            tableRows = listOf(
                listOf("1/1 (Full)", "0 stops", "100%", "GN 60m"),
                listOf("1/2", "-1 stop", "50%", "GN 42.4m"),
                listOf("1/4", "-2 stops", "25%", "GN 30m"),
                listOf("1/8", "-3 stops", "12.5%", "GN 21.2m"),
                listOf("1/16", "-4 stops", "6.25%", "GN 15m"),
                listOf("1/32", "-5 stops", "3.12%", "GN 10.6m"),
                listOf("1/64", "-6 stops", "1.56%", "GN 7.5m")
            )
        ),
        GuideTopic(
            title = "Ansel Adams Zone System (Zones 0 – X)",
            subtitle = "Precision tonal visualization for spot metering",
            summary = "Camera light meters always evaluate a scene to Zone V (18% neutral middle gray). If you point a spot meter at fresh white snow, the camera will underexpose it to gray unless you add +2 EV. If you point at black velvet, it will overexpose to gray unless you subtract -2 EV.",
            keyRules = listOf(
                "Zone 0: Maximum solid inkjet black; no detail.",
                "Zone II: Deepest textured black with faint shadow detail.",
                "Zone V: 18% neutral standard gray; skin tone of darker complexions.",
                "Zone VI: Average Caucasian skin tone (+1 EV above meter reading).",
                "Zone VII: Light textured skin, pale concrete, bright snow in shade (+2 EV).",
                "Zone VIII: Pure white with delicate texture (wedding gowns, sunlit snow; +3 EV).",
                "Zone X: Pure specular specular highlights; sun reflection on chrome or glass."
            ),
            tableHeaders = listOf("Zone", "EV Offset", "Tonal Description", "Common Real-World Subject"),
            tableRows = listOf(
                listOf("Zone 0", "-5 EV", "Pure black", "Deepest shadow / no light"),
                listOf("Zone II", "-3 EV", "Textured shadow", "Dark hair, black tuxedo fabric"),
                listOf("Zone IV", "-1 EV", "Dark foliage", "Deep green grass, shadow side of face"),
                listOf("Zone V", "0 EV", "18% Middle Gray", "Gray card, weathered wood, blue sky"),
                listOf("Zone VI", "+1 EV", "Bright skin", "Average Caucasian skin tone"),
                listOf("Zone VII", "+2 EV", "Light tones", "White wall in soft light, light skin"),
                listOf("Zone VIII", "+3 EV", "Textured white", "White wedding dress, snow in sunlight"),
                listOf("Zone X", "+5 EV", "Pure paper white", "Direct sun, specular water sparkles")
            )
        ),
        GuideTopic(
            title = "Neutral Density (ND) Filter Exposure Table",
            subtitle = "Calculating extended shutter speeds for silky water & wide apertures",
            summary = "ND filters reduce the volume of light entering the lens without altering color fidelity. This allows photographers to use ultra-wide apertures (f/1.2 or f/1.4) in blinding noon sun or execute multi-second long exposures for silky waterfalls and cloud movement.",
            keyRules = listOf(
                "1 Stop Cut (ND2 / 0.3): Doubles exposure time (e.g. 1/1000s -> 1/500s)",
                "3 Stops Cut (ND8 / 0.9): Ideal for shooting f/1.4 video at 1/50s in outdoor light",
                "6 Stops Cut (ND64 / 1.8): Great for slowing waterfalls in moderate ambient daylight",
                "10 Stops Cut (ND1000 / 3.0): Turns 1/125s daylight into an 8-second glass-water exposure!",
                "Formula: New Shutter Time = Base Shutter Time × 2^(ND Stops)"
            ),
            tableHeaders = listOf("Filter Name", "Optical Density", "Stops Reduction", "1/250s Base Becomes"),
            tableRows = listOf(
                listOf("ND2", "0.3", "1 stop", "1/125s"),
                listOf("ND4", "0.6", "2 stops", "1/60s"),
                listOf("ND8", "0.9", "3 stops", "1/30s"),
                listOf("ND16", "1.2", "4 stops", "1/15s"),
                listOf("ND32", "1.5", "5 stops", "1/8s"),
                listOf("ND64", "1.8", "6 stops", "1/4s"),
                listOf("ND256", "2.4", "8 stops", "1s"),
                listOf("ND1000", "3.0", "10 stops", "4s")
            )
        )
    )
}
