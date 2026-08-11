package org.shanerx.tradeshop.harness;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;

/**
 * Turns an aborted test into a failed one, at the point it aborts.
 *
 * <p>An abort is how MockBukkit says "this Bukkit call is not implemented":
 * {@code UnimplementedOperationException} extends {@link TestAbortedException},
 * so without this the suite reports {@code Tests run: 1, Skipped: 1} and the
 * build goes green having verified nothing.
 *
 * <p>The POM also fails the build on any {@code <skipped>} in the surefire
 * reports, which is the backstop and catches {@code @Disabled} and aborts raised
 * from extension callbacks too. This class exists on top of that so the report
 * names the test and carries the stack trace of the unimplemented call, instead
 * of the build simply stopping at the end with "something was skipped".
 *
 * <p>Registered for every test through {@code META-INF/services}, so a new test
 * class cannot forget it.
 */
public class SkippedTestsAreFailures implements InvocationInterceptor {

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> ctx, ExtensionContext ec) throws Throwable {
        failOnAbort(invocation);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> ctx, ExtensionContext ec) throws Throwable {
        failOnAbort(invocation);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> ctx, ExtensionContext ec) throws Throwable {
        failOnAbort(invocation);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> ctx, ExtensionContext ec) throws Throwable {
        failOnAbort(invocation);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> ctx, ExtensionContext ec) throws Throwable {
        failOnAbort(invocation);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> ctx, ExtensionContext ec) throws Throwable {
        failOnAbort(invocation);
    }

    private void failOnAbort(Invocation<Void> invocation) throws Throwable {
        try {
            invocation.proceed();
        } catch (TestAbortedException aborted) {
            throw asFailure(aborted);
        }
    }

    /** Kept package-visible so the mock server can reuse it from outside an invocation. */
    static AssertionError asFailure(TestAbortedException aborted) {
        AssertionError failure = new AssertionError(
                "Test aborted, which this suite counts as a failure: " + aborted.getMessage()
                        + "\nIf this is MockBukkit's UnimplementedOperationException, implement the call in "
                        + TradeShopServerMock.class.getSimpleName() + " rather than letting the test skip.",
                aborted);
        failure.setStackTrace(aborted.getStackTrace());
        return failure;
    }
}
