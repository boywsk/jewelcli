/*
 * Copyright 2006 Tim Wood
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.flamingpenguin.jewel.cli;

import static com.lexicalscope.fluentreflection.FluentReflection.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lexicalscope.fluentreflection.ReflectedClass;
import com.lexicalscope.fluentreflection.ReflectedMethod;

class OptionsSpecificationImpl<O> implements OptionsSpecification<O>, OptionsSpecificationBuilder, CliSpecification {
    private final ReflectedClass<O> m_klass;

    private final Map<String, OptionSpecification> m_optionsShortName = new HashMap<String, OptionSpecification>();
    private final Map<String, OptionSpecification> m_optionsLongName = new TreeMap<String, OptionSpecification>();
    private final Map<Method, OptionSpecification> m_optionsMethod = new HashMap<Method, OptionSpecification>();
    private final Map<ReflectedMethod, OptionSpecification> m_optionalOptionsMethod =
            new HashMap<ReflectedMethod, OptionSpecification>();

    private final Map<Method, OptionSpecification> m_unparsedOptionsMethod = new HashMap<Method, OptionSpecification>();
    private final Map<ReflectedMethod, OptionSpecification> m_unparsedOptionalOptionsMethod =
            new HashMap<ReflectedMethod, OptionSpecification>();

    private String m_applicationName;

    private OptionsSpecificationImpl(final ReflectedClass<O> klass) {
        m_klass = klass;
    }

    static <O> OptionsSpecificationImpl<O> createOptionsSpecificationImpl(final ReflectedClass<O> klass) {
        final OptionsSpecificationImpl<O> optionsSpecificationImpl = new OptionsSpecificationImpl<O>(klass);

        final OptionsSpecificationParser<O> optionsSpecificationParser = new OptionsSpecificationParser<O>(klass);
        optionsSpecificationParser.buildOptionsSpecification(optionsSpecificationImpl);

        return optionsSpecificationImpl;
    }

    /**
     * @{inheritdoc
     */
    public boolean isSpecified(final String key) {
        return m_optionsShortName.containsKey(key) || m_optionsLongName.containsKey(key);
    }

    /**
     * @{inheritdoc
     */
    public boolean isSpecified(final Method method) {
        return m_optionsMethod.containsKey(method) || m_optionalOptionsMethod.containsKey(method(method));
    }

    /**
     * @{inheritdoc
     */
    public OptionSpecification getSpecification(final String key) {
        if (m_optionsLongName.containsKey(key)) {
            return m_optionsLongName.get(key);
        } else {
            return m_optionsShortName.get(key);
        }
    }

    @Override public OptionSpecification getSpecification(final ReflectedMethod reflectedMethod) {
        return getSpecification(reflectedMethod.methodUnderReflection());
    }

    /**
     * @{inheritdoc
     */
    public OptionSpecification getSpecification(final Method method) {
        if (m_optionsMethod.containsKey(method)) {
            return m_optionsMethod.get(method);
        }
        return m_optionalOptionsMethod.get(method(method));
    }

    /**
     * @{inheritdoc
     */
    public List<OptionSpecification> getMandatoryOptions() {
        final List<OptionSpecification> result = new ArrayList<OptionSpecification>();
        for (final OptionSpecification specification : m_optionsLongName.values()) {
            if (!specification.isOptional() && !specification.hasDefaultValue()) {
                result.add(specification);
            }
        }

        return result;
    }

    public boolean isExistenceChecker(final ReflectedMethod method) {
        return m_optionalOptionsMethod.containsKey(method);
    }

    public Iterator<OptionSpecification> iterator() {
        return new ArrayList<OptionSpecification>(m_optionsMethod.values()).iterator();
    }

    public OptionSpecification getUnparsedSpecification() {
        return m_unparsedOptionsMethod.values().iterator().next();
    }

    public boolean hasUnparsedSpecification() {
        return !m_unparsedOptionsMethod.values().isEmpty();
    }

    public String getApplicationName() {
        if (m_applicationName == null || m_applicationName.trim().equals("")) {
            return m_klass.name();
        } else {
            return m_applicationName;
        }
    }

    public void addOption(final OptionSpecification optionSpecification) {
        for (final String shortName : optionSpecification.getShortNames()) {
            m_optionsShortName.put(shortName, optionSpecification);
        }

        m_optionsLongName.put(optionSpecification.getLongName(), optionSpecification);
        m_optionsMethod.put(optionSpecification.getMethod(), optionSpecification);

        if (optionSpecification.isOptional()) {
            m_optionalOptionsMethod.put(optionSpecification.getOptionalityMethod(), optionSpecification);
        }
    }

    public void addUnparsedOption(final OptionSpecification optionSpecification) {
        m_unparsedOptionsMethod.put(optionSpecification.getMethod(), optionSpecification);

        if (optionSpecification.isOptional()) {
            m_unparsedOptionalOptionsMethod.put(optionSpecification.getOptionalityMethod(), optionSpecification);
        }
    }

    public void setApplicationName(final String application) {
        m_applicationName = application;
    }

    public boolean isUnparsedExistenceChecker(final ReflectedMethod method) {
        return m_unparsedOptionalOptionsMethod.containsKey(method);
    }

    public boolean isUnparsedMethod(final Method method) {
        return m_unparsedOptionsMethod.containsKey(method);
    }

    @Override public String toString() {
        final StringBuilder message = new StringBuilder();
        if (nullOrBlank(m_applicationName) && !hasUnparsedSpecification()) {
            message.append("The options available are:");
        } else {
            message.append("Usage: ");

            if (!nullOrBlank(m_applicationName)) {
                message.append(String.format("%s ", m_applicationName.trim()));
            }

            if (getMandatoryOptions().isEmpty()) {
                message.append("[");
            }
            message.append("options");
            if (getMandatoryOptions().isEmpty()) {
                message.append("]");
            }

            if (hasUnparsedSpecification()) {
                message.append(" ");
                final String unparsedName =
                        !nullOrBlank(getUnparsedSpecification().getLongName()) ? getUnparsedSpecification()
                                .getLongName() : "ARGUMENTS";
                message.append(unparsedName);

                if (getUnparsedSpecification().isMultiValued()) {
                    message.append("...");
                }
            }
        }

        final String lineSeparator = System.getProperty("line.separator");
        message.append(lineSeparator);

        String separator = "";
        for (final OptionSpecification specification : m_optionsLongName.values()) {
            message.append(separator).append("\t").append(specification);
            separator = lineSeparator;
        }

        return message.toString();
    }

    private boolean nullOrBlank(final String string) {
        return string == null || string.trim().equals("");
    }
}
