package uk.co.flamingpenguin.jewel.cli;

/**
 * Parses arguments and presents them, in a typesafe style, as an
 * instance of the interface <code>O</code>
 *
 * @author Tim Wood
 *
 * @param <O> The type of interface provided by this Cli
 */
public interface Cli<O>
{
   /**
    * Parse the arguments and present them as an instance of the
    * interface O
    *
    * @param arguments The arguments that will be parsed
    *
    * @return An instance of the interface O which will present
    *         the parsed arguments
    *
    * @throws InvalidArgumentsException
    * @throws ArgumentValidationException
    */
   O parseArguments(final String[] arguments) throws InvalidArgumentsException, ArgumentValidationException;

}