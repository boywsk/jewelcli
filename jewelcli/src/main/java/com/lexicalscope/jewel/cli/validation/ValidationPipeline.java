//
//Author       : t.wood
//Copyright    : (c) Resilient Networks plc 2012 - All Rights Reserved
//
package com.lexicalscope.jewel.cli.validation;

import static com.lexicalscope.fluent.FluentDollar.$;
import static com.lexicalscope.jewel.cli.validation.RawOptionMatchers.isLastOption;

import java.util.List;
import java.util.Map.Entry;

import ch.lambdaj.function.convert.Converter;

import com.lexicalscope.fluent.functions.BiConverter;
import com.lexicalscope.fluent.map.transforms.MapPipelineBuilder;
import com.lexicalscope.jewel.cli.ValidationErrorBuilder;
import com.lexicalscope.jewel.cli.specification.OptionsSpecification;
import com.lexicalscope.jewel.cli.specification.ParsedOptionSpecification;

class ValidationPipeline
{
   private final OptionsSpecification<?> specification;
   private final ValidationErrorBuilder validationErrorBuilder;

   ValidationPipeline(final OptionsSpecification<?> specification, final ValidationErrorBuilder validationErrorBuilder)
   {
      this.specification = specification;
      this.validationErrorBuilder = validationErrorBuilder;
   }

   MapPipelineBuilder<RawOption, List<String>, ParsedOptionSpecification, List<String>> buildValidationPipeline(final List<String> validatedUnparsedArguments)
   {
      return $.<RawOption, List<String>>mapPipeline().
               allowKeys(thatAreKnownOptions()).
               processPuts(trimExcessOptionsInto(validatedUnparsedArguments)).
               convertKeys(lookupSpecifcationFromOption()).
               allowKeys(butRejectHelpOption()).
               vetoPuts(thatHaveTheWrongNumberOfValues()).
               vetoPuts(thatHaveValuesInTheWrongFormat());
   }

   private KnownOptions thatAreKnownOptions()
   {
      return new KnownOptions(specification, validationErrorBuilder);
   }

   private Converter<Entry<RawOption, List<String>>, Entry<RawOption, List<String>>> trimExcessOptionsInto(final List<String> validatedUnparsedArguments)
   {
      return $.predicatedConverter(isLastOption(), new TrimExccessValues(specification, validatedUnparsedArguments));
   }

   private BiConverter<ParsedOptionSpecification, RawOption> lookupSpecifcationFromOption()
   {
      return new ConverterRawOptionToParsedOptionSpecification(specification);
   }

   private ReportWrongFormatValues thatHaveValuesInTheWrongFormat()
   {
      return new ReportWrongFormatValues(validationErrorBuilder);
   }

   private ReportWrongNumberOfValues thatHaveTheWrongNumberOfValues()
   {
      return new ReportWrongNumberOfValues(validationErrorBuilder);
   }

   private RejectHelpOption butRejectHelpOption()
   {
      return new RejectHelpOption(specification);
   }
}
