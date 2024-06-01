package com.nextdevv.kgui2.annotations

// Warn the user that the annotated element is experimental
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Experimental(val message: String = "This feature is experimental and may be changed or removed in future versions.")


