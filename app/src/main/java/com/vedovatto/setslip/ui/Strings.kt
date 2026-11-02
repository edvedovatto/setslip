package com.vedovatto.setslip.ui

import androidx.compose.runtime.compositionLocalOf
import com.vedovatto.setslip.domain.Equipment
import com.vedovatto.setslip.domain.Language
import com.vedovatto.setslip.domain.Level
import com.vedovatto.setslip.domain.MuscleGroup
import com.vedovatto.setslip.domain.Workout

class Strings(
    val language: Language,
    private val groupNames: Map<MuscleGroup, String>,
    private val groupCodes: Map<MuscleGroup, String>,
    private val equipmentNames: Map<Equipment, String>,
    private val levelNames: Map<Level, String>,
    private val muscleNames: Map<String, String>,
    val print: String,
    val another: String,
    val random: String,
    val slip: String,
    private val exercisesWord: String,
    private val doneFormat: String,
    val cardio: String,
    val howTo: String,
    val share: String,
    val useSlip: String,
    val hintEmpty: String,
    val tipTitle: String,
    val tips: List<Pair<String, String>>,
    val gotIt: String,
    val undo: String,
    val crossedOut: String,
    val favorites: String,
    val settings: String,
    val back: String,
    val steps: String,
    val prescription: String,
    val level: String,
    val equipment: String,
    val muscles: String,
    val languageTitle: String,
    val sound: String,
    val vibration: String,
    val on: String,
    val off: String,
    val crossedOutTitle: String,
    val crossedOutEmpty: String,
    val crossedOutHint: String,
    val about: String,
    val aboutText: String,
    val credits: String,
    val creditsText: String,
    val emptyFavorites: String,
    val emptyFilter: String,
    val filterHint: String,
    val addFavorite: String,
    val removeFavorite: String,
    val mute: String,
    val unmute: String,
    val indexHint: String,
) {
    fun group(g: MuscleGroup) = groupNames.getValue(g)
    fun code(g: MuscleGroup) = groupCodes.getValue(g)
    fun equipment(e: Equipment) = equipmentNames.getValue(e)
    fun level(l: Level) = levelNames.getValue(l)
    fun muscle(m: String) = muscleNames[m] ?: m
    fun title(w: Workout) = w.groups.joinToString(" + ") { group(it).uppercase() }
    fun exercises(n: Int) = "$n $exercisesWord"
    fun done(done: Int, total: Int) = doneFormat.format(done, total)
}

private val ptGroups = mapOf(
    MuscleGroup.CHEST to "Peito", MuscleGroup.BACK to "Costas", MuscleGroup.SHOULDERS to "Ombro",
    MuscleGroup.BICEPS to "Bíceps", MuscleGroup.TRICEPS to "Tríceps", MuscleGroup.QUADRICEPS to "Quadríceps",
    MuscleGroup.HAMSTRINGS to "Posterior", MuscleGroup.GLUTES to "Glúteo", MuscleGroup.ABS to "Abdômen",
)

private val enGroups = mapOf(
    MuscleGroup.CHEST to "Chest", MuscleGroup.BACK to "Back", MuscleGroup.SHOULDERS to "Shoulders",
    MuscleGroup.BICEPS to "Biceps", MuscleGroup.TRICEPS to "Triceps", MuscleGroup.QUADRICEPS to "Quads",
    MuscleGroup.HAMSTRINGS to "Hamstrings", MuscleGroup.GLUTES to "Glutes", MuscleGroup.ABS to "Abs",
)

private val enCodes = mapOf(
    MuscleGroup.CHEST to "CHE", MuscleGroup.BACK to "BAC", MuscleGroup.SHOULDERS to "SHO",
    MuscleGroup.BICEPS to "BIC", MuscleGroup.TRICEPS to "TRI", MuscleGroup.QUADRICEPS to "QUA",
    MuscleGroup.HAMSTRINGS to "HAM", MuscleGroup.GLUTES to "GLU", MuscleGroup.ABS to "ABS",
)

val PtStrings = Strings(
    language = Language.PT,
    groupNames = ptGroups,
    groupCodes = MuscleGroup.entries.associateWith { it.code },
    equipmentNames = mapOf(
        Equipment.BARBELL to "Barra", Equipment.DUMBBELL to "Halteres", Equipment.CABLE to "Polia",
        Equipment.MACHINE to "Máquina", Equipment.BODY to "Peso do corpo", Equipment.EZ_BAR to "Barra W",
        Equipment.KETTLEBELL to "Kettlebell", Equipment.OTHER to "Outro",
    ),
    levelNames = mapOf(Level.BEGINNER to "Iniciante", Level.INTERMEDIATE to "Intermediário", Level.EXPERT to "Avançado"),
    muscleNames = mapOf(
        "chest" to "peito", "shoulders" to "ombros", "triceps" to "tríceps", "lats" to "dorsais",
        "middle back" to "meio das costas", "biceps" to "bíceps", "forearms" to "antebraços",
        "quadriceps" to "quadríceps", "hamstrings" to "posterior", "glutes" to "glúteos",
        "abdominals" to "abdômen", "calves" to "panturrilhas", "lower back" to "lombar", "traps" to "trapézio",
        "adductors" to "adutores", "abductors" to "abdutores", "neck" to "pescoço",
    ),
    print = "IMPRIMIR",
    another = "OUTRA",
    random = "ALEATÓRIO",
    slip = "FICHA",
    exercisesWord = "EXERCÍCIOS",
    doneFormat = "%d DE %d FEITOS",
    cardio = "+ CÁRDIO À ESCOLHA (OPCIONAL)",
    howTo = "COMO FAZER",
    share = "compartilhar ficha",
    useSlip = "USAR ESTA",
    hintEmpty = "ESCOLHA ATÉ 3 GRUPOS\nE APERTE IMPRIMIR",
    tipTitle = "PRIMEIRA FICHA",
    tips = listOf(
        "TOQUE" to " no exercício para marcar feito",
        "SEGURE" to " para riscar: ele não volta mais",
        "ESTRELA" to " guarda a ficha nas favoritas",
        "?" to " mostra como fazer cada exercício",
    ),
    gotIt = "ENTENDI",
    undo = "DESFAZER",
    crossedOut = "RISCADO",
    favorites = "FAVORITAS",
    settings = "AJUSTES",
    back = "VOLTAR",
    steps = "PASSO A PASSO",
    prescription = "SÉRIES X REPS",
    level = "NÍVEL",
    equipment = "EQUIPAMENTO",
    muscles = "MÚSCULOS",
    languageTitle = "IDIOMA",
    sound = "SOM",
    vibration = "VIBRAÇÃO",
    on = "LIGADO",
    off = "DESLIGADO",
    crossedOutTitle = "EXERCÍCIOS RISCADOS",
    crossedOutEmpty = "Nenhum exercício riscado.",
    crossedOutHint = "toque para desfazer",
    about = "SOBRE",
    aboutText = "Setslip imprime fichas de treino. Escolha até 3 grupos e aperte imprimir.",
    credits = "CRÉDITOS",
    creditsText = "Fotos e dados dos exercícios: free-exercise-db (domínio público). Fonte: DM Mono (OFL).",
    emptyFavorites = "Nenhuma ficha favorita ainda. Toque na estrela de uma ficha.",
    emptyFilter = "Nenhuma favorita com esses grupos.",
    filterHint = "FILTRAR POR GRUPO",
    addFavorite = "favoritar ficha",
    removeFavorite = "remover dos favoritos",
    mute = "silenciar",
    unmute = "ligar som",
    indexHint = "toque num exercício",
)

val EnStrings = Strings(
    language = Language.EN,
    groupNames = enGroups,
    groupCodes = enCodes,
    equipmentNames = mapOf(
        Equipment.BARBELL to "Barbell", Equipment.DUMBBELL to "Dumbbells", Equipment.CABLE to "Cable",
        Equipment.MACHINE to "Machine", Equipment.BODY to "Bodyweight", Equipment.EZ_BAR to "EZ bar",
        Equipment.KETTLEBELL to "Kettlebell", Equipment.OTHER to "Other",
    ),
    levelNames = mapOf(Level.BEGINNER to "Beginner", Level.INTERMEDIATE to "Intermediate", Level.EXPERT to "Expert"),
    muscleNames = emptyMap(),
    print = "PRINT",
    another = "ANOTHER",
    random = "RANDOM",
    slip = "SLIP",
    exercisesWord = "EXERCISES",
    doneFormat = "%d OF %d DONE",
    cardio = "+ CARDIO OF YOUR CHOICE (OPTIONAL)",
    howTo = "HOW TO",
    share = "share slip",
    useSlip = "USE THIS ONE",
    hintEmpty = "PICK UP TO 3 GROUPS\nAND HIT PRINT",
    tipTitle = "FIRST SLIP",
    tips = listOf(
        "TAP" to " an exercise to check it off",
        "HOLD" to " to cross it out: it never returns",
        "STAR" to " saves the slip to favorites",
        "?" to " shows how to do each exercise",
    ),
    gotIt = "GOT IT",
    undo = "UNDO",
    crossedOut = "CROSSED OUT",
    favorites = "FAVORITES",
    settings = "SETTINGS",
    back = "BACK",
    steps = "STEP BY STEP",
    prescription = "SETS X REPS",
    level = "LEVEL",
    equipment = "EQUIPMENT",
    muscles = "MUSCLES",
    languageTitle = "LANGUAGE",
    sound = "SOUND",
    vibration = "VIBRATION",
    on = "ON",
    off = "OFF",
    crossedOutTitle = "CROSSED-OUT EXERCISES",
    crossedOutEmpty = "No crossed-out exercises.",
    crossedOutHint = "tap to undo",
    about = "ABOUT",
    aboutText = "Setslip prints workout slips. Pick up to 3 muscle groups and hit print.",
    credits = "CREDITS",
    creditsText = "Exercise photos and data: free-exercise-db (public domain). Font: DM Mono (OFL).",
    emptyFavorites = "No favorite slips yet. Tap the star on a slip.",
    emptyFilter = "No favorites with those groups.",
    filterHint = "FILTER BY GROUP",
    addFavorite = "favorite slip",
    removeFavorite = "remove from favorites",
    mute = "mute",
    unmute = "unmute",
    indexHint = "tap an exercise",
)

fun stringsFor(language: Language) = if (language == Language.EN) EnStrings else PtStrings

val LocalStrings = compositionLocalOf { PtStrings }
