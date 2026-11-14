package com.vedovatto.setslip.domain

enum class GroupSize { LARGE, SMALL }

enum class MuscleGroup(val size: GroupSize) {
    CHEST(GroupSize.LARGE),
    BACK(GroupSize.LARGE),
    SHOULDERS(GroupSize.SMALL),
    BICEPS(GroupSize.SMALL),
    TRICEPS(GroupSize.SMALL),
    QUADRICEPS(GroupSize.LARGE),
    HAMSTRINGS(GroupSize.LARGE),
    GLUTES(GroupSize.LARGE),
    ABS(GroupSize.SMALL),
}
