package org.hamidelmaazouz.graaljulia.runtime;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.utilities.TriState;
import org.hamidelmaazouz.graaljulia.SLLanguage;

/**
 * The SL type for a {@code null} (i.e., undefined) value. In Truffle, it is generally discouraged
 * to use the Java {@code null} value to represent the guest language {@code null} value. It is not
 * possible to specialize on Java {@code null} (since you cannot ask it for the Java class), and
 * there is always the danger of a spurious {@link NullPointerException}. Representing the guest
 * language {@code null} as a singleton, as in {@link #SINGLETON this class}, is the recommended
 * practice.
 */
@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
public final class SLNull implements TruffleObject {

    /**
     * The canonical value to represent {@code null} in SL.
     */
    public static final SLNull SINGLETON = new SLNull();
    private static final int IDENTITY_HASH = System.identityHashCode(SINGLETON);

    /**
     * Disallow instantiation from outside to ensure that the {@link #SINGLETON} is the only
     * instance.
     */
    private SLNull() {
    }

    /**
     * This method is, e.g., called when using the {@code null} value in a string concatenation. So
     * changing it has an effect on SL programs.
     */
    @Override
    public String toString() {
        return "NULL";
    }

    @ExportMessage
    boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    Class<? extends TruffleLanguage<?>> getLanguage() {
        return SLLanguage.class;
    }

    /**
     * {@link SLNull} values are interpreted as null values by other languages.
     */
    @ExportMessage
    boolean isNull() {
        return true;
    }

    @ExportMessage
    boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    Object getMetaObject() {
        return SLType.NULL;
    }

    @ExportMessage
    static TriState isIdenticalOrUndefined(@SuppressWarnings("unused") SLNull receiver, Object other) {
        /*
         * SLNull values are identical to other SLNull values.
         */
        return TriState.valueOf(SLNull.SINGLETON == other);
    }

    @ExportMessage
    static int identityHashCode(@SuppressWarnings("unused") SLNull receiver) {
        /*
         * We do not use 0, as we want consistency with System.identityHashCode(receiver).
         */
        return IDENTITY_HASH;
    }

    @ExportMessage
    Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
        return "NULL";
    }
}
