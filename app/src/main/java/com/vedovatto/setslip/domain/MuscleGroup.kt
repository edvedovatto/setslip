package com.vedovatto.setslip.domain

enum class GroupSize { LARGE, SMALL }

enum class MuscleGroup(val code: String, val label: String, val size: GroupSize) {
    CHEST("PEI", "Peito", GroupSize.LARGE),
    BACK("COS", "Costas", GroupSize.LARGE),
    SHOULDERS("OMB", "Ombro", GroupSize.SMALL),
    BICEPS("BIC", "Bíceps", GroupSize.SMALL),
    TRICEPS("TRI", "Tríceps", GroupSize.SMALL),
    QUADRICEPS("QUA", "Quadríceps", GroupSize.LARGE),
    HAMSTRINGS("POS", "Posterior", GroupSize.LARGE),
    GLUTES("GLU", "Glúteo", GroupSize.LARGE),
    CALVES("PAN", "Panturrilha", GroupSize.SMALL),
    ABS("ABD", "Abdômen", GroupSize.SMALL),
}
