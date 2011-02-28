package com.ovea.jetty.session.serializer.jboss.serial.io;

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetaData;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.DataContainer;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.safecloning.SafeCloningRepository;
import org.apache.log4j.Logger;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This is the equivalent for MarshalledObject on RMI, but this is optimized for local calls.
 * Instead of converting every single field into Bytes, this will use a DataContainer. 
 * @author Clebert Suconic
 */
public class MarshalledObjectForLocalCalls implements Externalizable
{
	private static final long serialVersionUID = 785809358605094514L;

	private static final Logger log = Logger.getLogger(ClassMetaData.class);
   	private static final boolean isDebug = log.isDebugEnabled();
	

	   DataContainer container;

	   public MarshalledObjectForLocalCalls()
	   {
	   }

	   public MarshalledObjectForLocalCalls(Object obj) throws IOException
	   {
	      container = new DataContainer(false);
	      ObjectOutput output = container.getOutput();
	      output.writeObject(obj);
	      output.flush();
	      container.flush();
	   }

	   public MarshalledObjectForLocalCalls(Object obj, SafeCloningRepository safeToReuse) throws IOException
	   {
	      container = new DataContainer(null, null, safeToReuse, false,null);
	      ObjectOutput output = container.getOutput();
	      output.writeObject(obj);
	      output.flush();
	      container.flush();
	   }

	   /**
	    * The object has to be unserialized only when the first get is executed.
	    */
	   public Object get() throws IOException, ClassNotFoundException
	   {
	      try
	      {
	         container.getCache().setLoader(Thread.currentThread().getContextClassLoader());
	         return container.getInput().readObject();
	      }
	      catch(RuntimeException e)
	      {
	         log.error(e, e);
	         throw e;
	      }
	   }

	   public void writeExternal(ObjectOutput out) throws IOException
	   {
		  if (isDebug)
		  {
			  log.debug("writeExternal");
		  }
		  container.saveData(out);
	   }

	   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	   {
		  if (isDebug)
		  {
			  log.debug("readExternal");
		  }
	      container = new DataContainer(false);
	      container.loadData(in);
	   }
}
