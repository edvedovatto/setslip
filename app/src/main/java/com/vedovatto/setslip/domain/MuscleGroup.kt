package com.vedovatto.setslip.domain

enum class GroupSize { LARGE, SMALL }

enum class MuscleGroup(val code: String, val size: GroupSize) {
    CHEST("PEI", GroupSize.LARGE),
    BACK("COS", GroupSize.LARGE),
    SHOULDERS("OMB", GroupSize.SMALL),
    BICEPS("BIC", GroupSize.SMALL),
    TRICEPS("TRI", GroupSize.SMALL),
    QUADRICEPS("QUA", GroupSize.LARGE),
    HAMSTRINGS("POS", GroupSize.LARGE),
    GLUTES("GLU", GroupSize.LARGE),
    ABS("ABD", GroupSize.SMALL),
}
