package me.winter.ai;

import me.winter.ai.word.*;
import org.jibble.pircbot.PircBot;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * Created by Alexander Winter on 2015-12-01.
 */
public class AIBot extends PircBot implements User
{
	private Memory memory;

	public AIBot()
	{
		//super.setName("AIBot-" + UUID.randomUUID().toString().substring(0, 5));
		super.setName("Verbot");
		this.memory = new Memory(this);
	}

	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		if(message.startsWith(".wasitvalid"))
		{
			sendMessage(channel, this.memory.getLastSentenceBy(null).isValid() ? "Yes" : "No");

			return;
		}

		if(message.startsWith(".whattype"))
		{
			sendMessage(channel, this.memory.getLastSentenceBy(null).getType().name().toLowerCase());

			return;
		}

		if(message.startsWith(".isverb"))
		{
			String[] parts = message.split(" ");

			if(parts.length < 3)
			{
				this.sendMessage(channel, sender + ": Too few arguments. (.isverb <verb> <conjugated-verb... >)");
				return;
			}

			Verb verb;
			try
			{
				verb = IrregularVerb.valueOf(parts[1].toUpperCase());
			}
			catch(Exception e)
			{
				verb = new RegularVerb(parts[1]);
			}

			String toAnalyze = parts[2];

			for(int i = 3; i < parts.length; i++)
				toAnalyze += " " + parts[i];

			this.sendMessage(channel, sender + ": " + verb.isVerb(toAnalyze));

			return;
		}

		if(message.startsWith(".listusers"))
		{
			for(User user : this.memory.getUsers())
				sendMessage(channel, user.getOriginalName());

			return;
		}

		if(message.startsWith(".listsentences"))
		{
			for(Sentence sentence : this.memory.getSentencesBy(null))
				sendMessage(channel, sentence.getRaw());

			return;
		}

		if(message.startsWith(".getperson"))
		{
			String[] parts = message.split(" ");

			if(parts.length < 2)
			{
				this.sendMessage(channel, sender + ": Too few arguments. (.getperson <pronoun(s)... >)");
				return;
			}

			String toAnalyze = parts[1];

			for(int i = 2; i < parts.length; i++)
				toAnalyze += " " + parts[i];

			sendMessage(channel, sender + ": " + EnglishUtil.getPerson(toAnalyze) + " (" + EnglishUtil.getPronoun(EnglishUtil.getPerson(toAnalyze)) + ")");

			return;
		}

		if(message.startsWith(".vlist"))
		{
			String[] args = message.split(" ");

			if(args.length < 2)
			{
				this.sendMessage(channel, sender + ": Too few arguments. (.list <tense|type>)");
				return;
			}

			if(args[1].equalsIgnoreCase("tense"))
			{
				String tenses = "Availables tenses: ";
				for(VerbTense tense : VerbTense.values())
					tenses += tense.name().toLowerCase().replace('_', '-') + ", ";
				tenses = tenses.substring(0, tenses.length() - 2);
				this.sendMessage(channel, sender + ": " + tenses);
				return;
			}

			if(args[1].equalsIgnoreCase("type"))
			{
				String types = "Availables conjugation types: ";
				for(ConjugationType type : ConjugationType.values())
					types += type.name().toLowerCase().replace('_', '-') + ", ";
				types = types.substring(0, types.length() - 2);
				this.sendMessage(channel, sender + ": " + types);
				return;
			}

			this.sendMessage(channel, sender + ": Bad argument from a bad person. (.list <tense|type>)");
			return;
		}

		if(message.startsWith(".verbexamples"))
		{
			if(message.split(" ").length < 2)
			{
				this.sendMessage(channel, sender + ": Too few arguments. (.verbexamples <verb> [count])");
				return;
			}

			Verb verb;
			try
			{
				verb = IrregularVerb.valueOf(message.split(" ")[1].toUpperCase());
			}
			catch(Exception e)
			{
				verb = new RegularVerb(message.split(" ")[1]);
			}

			int count;
			try
			{
				count = Integer.parseInt(message.split(" ")[2]);
			}
			catch(Exception e)
			{
				count = 3;
			}
			if(count > 10)
				count = 10;

			for(int i = 0; i < count; i++)
			{
				VerbTense tense = VerbTense.values()[new Random().nextInt(VerbTense.values().length)];
				ConjugationType type = ConjugationType.values()[new Random().nextInt(ConjugationType.values().length)];
				int person = new Random().nextInt(6) + 1;

				this.sendMessage(channel, sender + ": To " + verb.getInfinitive() + ", "
								+ EnglishUtil.capitalize(type.name().toLowerCase().replaceAll("_", " ")) + " "
								+ EnglishUtil.capitalize(tense.name().toLowerCase().replaceAll("_", " ")) + " at "
								+ (((person - 1) % 3) + 1) + " person of " + (person < 4 ? "singular" : "plural"));
				this.sendMessage(channel, verb.conjugate(tense, type, person, null));
			}

			return;
		}

		if(message.startsWith(".conjugate") || message.startsWith(".cj"))
		{
			String[] args = message.split(" ");

			if(args.length < 5)
			{
				sendMessage(channel, sender + ": Not enough arguments. (.conjugate <verb> <verb-tense> <conjug-type> <person#|pronoun>)");
				return;
			}

			Verb verb;
			VerbTense tense;
			ConjugationType type;
			int person;
			String customName = null;

			try
			{
				verb = IrregularVerb.valueOf(args[1].toUpperCase());
			}
			catch(Exception e)
			{
				verb = new RegularVerb(args[1]);
			}

			try
			{
				tense = VerbTense.valueOf(args[2].toUpperCase().replace("-", "_"));
			}
			catch(Exception e)
			{
				sendMessage(channel, sender + ": I don't know this verb tense sorry.");
				return;
			}

			try
			{
				type = ConjugationType.valueOf(args[3].toUpperCase().replace("-", "_"));
			}
			catch(Exception e)
			{
				sendMessage(channel, sender + ": I don't know this conjugating type sorry.");
				return;
			}

			try
			{
				person = Integer.parseInt(args[4]);
			}
			catch(Exception e)
			{
				person = EnglishUtil.getPerson(customName = args[4]);
			}

			if(person <= 0 || person > 6)
			{
				sendMessage(channel, sender + ": " + person + " is not a valid person in english. (1 to 6)");
				return;
			}

			String rest = "";

			for(int i = 5; i < args.length; i++)
				rest += args[i] + " ";

			String result = verb.conjugate(tense, type, person, customName);

			if(!rest.equals(""))
			{
				rest = rest.substring(0, rest.length() - 1) + result.charAt(result.length() - 1);
				result = result.substring(0, result.length() - 1) + " " + rest;
			}


			this.sendMessage(channel, sender + ": " + result);
			return;
		}

		if(message.startsWith(".getsubject"))
		{
			String[] args = message.split(" ");

			if(args.length < 4)
			{
				sendMessage(channel, sender + ": Not enough arguments. (.getsubject <verb> <verb-tense> <conjug-type> <conjugated verb...>)");
				return;
			}

			Verb verb;
			VerbTense tense;
			ConjugationType type;

			try
			{
				verb = IrregularVerb.valueOf(args[1].toUpperCase().replace("-", "_"));
			}
			catch(Exception e)
			{
				verb = new RegularVerb(args[1]);
			}

			try
			{
				tense = VerbTense.valueOf(args[2].toUpperCase().replace("-", "_"));
			}
			catch(Exception e)
			{
				sendMessage(channel, sender + ": I don't know this verb tense sorry.");
				return;
			}

			try
			{
				type = ConjugationType.valueOf(args[3].toUpperCase().replace("-", "_"));
			}
			catch(Exception e)
			{
				sendMessage(channel, sender + ": I don't know this conjugating type sorry.");
				return;
			}

			String conjugatedVerb = args[4];

			for(int i = 5; i < args.length; i++)
				conjugatedVerb += " " + args[i];

			String result = verb.getSubject(tense, type, conjugatedVerb);

			this.sendMessage(channel, sender + ": " + result);
			return;
		}

		this.memory.receive(sender, message);
	}

	@Override
	public String getOriginalName()
	{
		return this.getName();
	}

	@Override
	public List<String> getAliases()
	{
		return Collections.emptyList();
	}
}
