package uk.co.flamingpenguin.jewel.cli;

import java.util.List;
import java.util.Map;

interface Arguments extends Iterable<Map.Entry<String, List<String>>> 
{
   List<String> getUnparsed();
   boolean contains(String ... options);
}