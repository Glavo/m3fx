// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.build;

import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;
import jdk.javadoc.internal.tool.DocEnvImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Set;

/// Runs the standard Javadoc doclet without its automatic JavaFX property merging.
///
/// The standard doclet automatically enables JavaFX mode whenever `javafx.base` is
/// visible. M3FX documents each property field and accessor explicitly, so that
/// inference would discard accessor-specific contracts and report the two valid
/// comments as duplicates. This adapter preserves every other standard-doclet
/// behavior while making only that capability probe report unavailable.
@NotNullByDefault
public final class M3FXStandardDoclet implements Doclet {
    /// The standard doclet that performs documentation generation.
    private final StandardDoclet delegate = new StandardDoclet();

    /// Creates a doclet adapter.
    public M3FXStandardDoclet() {
    }

    /// Initializes the delegated standard doclet.
    ///
    /// @param locale the documentation locale
    /// @param reporter the diagnostic reporter
    @Override
    public void init(Locale locale, Reporter reporter) {
        delegate.init(locale, reporter);
    }

    /// Returns the delegated doclet name.
    ///
    /// @return the doclet name
    @Override
    public String getName() {
        return delegate.getName();
    }

    /// Returns all options supported by the standard doclet.
    ///
    /// @return the supported options
    @Override
    public Set<? extends Option> getSupportedOptions() {
        return delegate.getSupportedOptions();
    }

    /// Returns the latest source version supported by the standard doclet.
    ///
    /// @return the supported source version
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return delegate.getSupportedSourceVersion();
    }

    /// Generates documentation using an environment that disables automatic
    /// JavaFX property inference.
    ///
    /// @param environment the documentation environment
    /// @return `true` when generation succeeds
    @Override
    public boolean run(DocletEnvironment environment) {
        return delegate.run(new ExplicitPropertyEnvironment(environment));
    }

    /// Delegates a documentation environment while masking the JavaFX capability probe.
    private static final class ExplicitPropertyEnvironment extends DocEnvImpl {
        /// The original documentation environment.
        private final DocletEnvironment delegate;

        /// An element utility view that masks `javafx.beans.Observable` lookup.
        private final Elements elements;

        /// Creates an environment view over the given delegate.
        ///
        /// @param delegate the original documentation environment
        private ExplicitPropertyEnvironment(DocletEnvironment environment) {
            this((DocEnvImpl) environment);
        }

        /// Creates an environment view over the standard doclet's environment.
        ///
        /// @param delegate the original standard-doclet environment
        private ExplicitPropertyEnvironment(DocEnvImpl delegate) {
            super(delegate.toolEnv, delegate.etable);
            this.delegate = delegate;
            this.elements = (Elements) Proxy.newProxyInstance(
                    Elements.class.getClassLoader(),
                    new Class<?>[]{Elements.class},
                    new ObservableMask(delegate.getElementUtils()));
        }

        /// Returns the originally specified elements.
        ///
        /// @return the specified elements
        @Override
        public Set<? extends Element> getSpecifiedElements() {
            return delegate.getSpecifiedElements();
        }

        /// Returns the originally included elements.
        ///
        /// @return the included elements
        @Override
        public Set<? extends Element> getIncludedElements() {
            return delegate.getIncludedElements();
        }

        /// Returns the original documentation tree utility.
        ///
        /// @return the documentation tree utility
        @Override
        public DocTrees getDocTrees() {
            return delegate.getDocTrees();
        }

        /// Returns the element utility view used to suppress JavaFX inference.
        ///
        /// @return the masked element utility
        @Override
        public Elements getElementUtils() {
            return elements;
        }

        /// Returns the original type utility.
        ///
        /// @return the type utility
        @Override
        public Types getTypeUtils() {
            return delegate.getTypeUtils();
        }

        /// Reports whether an element is included in the documentation.
        ///
        /// @param element the element to test
        /// @return `true` if the element is included
        @Override
        public boolean isIncluded(Element element) {
            return delegate.isIncluded(element);
        }

        /// Reports whether an element is selected for documentation.
        ///
        /// @param element the element to test
        /// @return `true` if the element is selected
        @Override
        public boolean isSelected(Element element) {
            return delegate.isSelected(element);
        }

        /// Returns the original Java file manager.
        ///
        /// @return the Java file manager
        @Override
        public JavaFileManager getJavaFileManager() {
            return delegate.getJavaFileManager();
        }

        /// Returns the source version used for this documentation run.
        ///
        /// @return the source version
        @Override
        public SourceVersion getSourceVersion() {
            return delegate.getSourceVersion();
        }

        /// Returns the module mode used for this documentation run.
        ///
        /// @return the module mode
        @Override
        public ModuleMode getModuleMode() {
            return delegate.getModuleMode();
        }

        /// Returns the source file kind for the given type.
        ///
        /// @param type the type to inspect
        /// @return the source file kind
        @Override
        public JavaFileObject.Kind getFileKind(TypeElement type) {
            return delegate.getFileKind(type);
        }
    }

    /// Masks only the standard doclet's lookup for `javafx.beans.Observable`.
    private static final class ObservableMask implements InvocationHandler {
        /// The original element utility.
        private final Elements delegate;

        /// Creates a masking invocation handler.
        ///
        /// @param delegate the original element utility
        private ObservableMask(Elements delegate) {
            this.delegate = delegate;
        }

        /// Delegates an element-utility invocation unless it is the JavaFX
        /// capability probe.
        ///
        /// @param proxy the generated proxy instance
        /// @param method the invoked interface method
        /// @param arguments the invocation arguments, or `null` when there are none
        /// @return the delegated result, or `null` for the capability probe
        /// @throws Throwable if the delegated invocation fails
        @Override
        public @Nullable Object invoke(Object proxy, Method method, @Nullable Object @Nullable [] arguments)
                throws Throwable {
            if (method.getName().equals("getTypeElement") && arguments != null) {
                Object name = arguments[arguments.length - 1];
                if (name instanceof CharSequence sequence && sequence.toString().equals("javafx.beans.Observable")) {
                    return null;
                }
            }

            try {
                return method.invoke(delegate, arguments);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }
    }
}
