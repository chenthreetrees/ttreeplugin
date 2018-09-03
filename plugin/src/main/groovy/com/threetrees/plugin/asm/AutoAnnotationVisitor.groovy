package com.threetrees.plugin.asm

import org.objectweb.asm.AnnotationVisitor

public class AutoAnnotationVisitor extends AnnotationVisitor {
    String mAnnotationName

    AutoAnnotationVisitor(int api, AnnotationVisitor av,String annotationName) {
        super(api, av)
        mAnnotationName = annotationName
    }
}