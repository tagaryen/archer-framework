package com.archer.framework.base.async;

import java.util.Arrays;

import com.archer.framework.base.annotation.Async;
import com.archer.tools.bytecode.AttributeInfo;
import com.archer.tools.bytecode.AttributeInfo.CodeAttribute;
import com.archer.tools.bytecode.AttributeInfo.CodeAttributeWriter;
import com.archer.tools.bytecode.AttributeInfo.ExceptionTable;
import com.archer.tools.bytecode.AttributeInfo.StackMapAttributeWriter;
import com.archer.tools.bytecode.AttributeInfo.StackMapEntry;
import com.archer.tools.bytecode.BytecodeException;
import com.archer.tools.bytecode.ClassBytecode;
import com.archer.tools.bytecode.MemberInfo;
import com.archer.tools.bytecode.constantpool.ConstantInfo;
import com.archer.tools.bytecode.constantpool.ConstantPool;
import com.archer.tools.bytecode.constantpool.ConstantUtf8;
import com.archer.tools.bytecode.util.DescriptorUtil;


public class AsyncProxy {
	
	private String superClassName;
	private String superRawClassName;
	private MemberInfo[] superMethods;
	private ConstantInfo[] supercp;
	private String classSimpleName;
	private String className;
	private String rawClassName;
	private String taskClassSimpleName;
	private String taskClassName;
	private String asyncType = "L" + DescriptorUtil.replaceDot2Slash(Async.class.getName()) + ";";
	
	
	public AsyncProxy(ClassBytecode superCls) {
		this.superClassName = superCls.getClassName();
		this.superRawClassName = DescriptorUtil.replaceDot2Slash(this.superClassName);
		this.superMethods = superCls.getMethods();
		this.supercp = superCls.getConstantPool().getCpInfo();
		this.classSimpleName = superCls.getSimpleName() + "$ArcherProxy";
		this.className = superClassName + "$ArcherProxy";
		this.rawClassName = DescriptorUtil.replaceDot2Slash(this.className);
		this.taskClassSimpleName = superCls.getSimpleName() + "$ArcherTask";
		this.taskClassName = superClassName + "$ArcherTask";
	}
	
	public Class<?> newAsyncClass() {
		int off = 0;
		String[] overrideMethodNames = new String[superMethods.length];
		String[] overrideMethodDescs = new String[superMethods.length];
		String pkg = this.superClassName.substring(0, this.superClassName.lastIndexOf('.'));
		for(int i = 0; i < superMethods.length; i++) {
			MemberInfo m = superMethods[i];
			if("RuntimeVisibleAnnotations".equals(m.getAttributes()[0].getName())) {
				int a0index = m.getAttributes()[0].getInfo()[2], a1index = m.getAttributes()[0].getInfo()[3];
				a0index = a0index < 0 ? a0index + 256 : a0index;
				a1index = a1index < 0 ? a1index + 256 : a1index;
				int aindex = (a0index << 8) | a1index;
				if(asyncType.equals(((ConstantUtf8)supercp[aindex]).getValue())) {
					String name = taskClassSimpleName + (off + 1);
					overrideMethodNames[off] = m.getName();
					overrideMethodDescs[off] = m.getDesc();
					ClassBytecode taskClassBytecode = generateAsyncTask(name, pkg, m.getName(), m.getDesc());
					taskClassBytecode.loadSelfClass();
					off++;
				}
			}
		}
		ClassBytecode newClassByteCode = generateAsyncClassCode(pkg, Arrays.copyOfRange(overrideMethodNames, 0, off), Arrays.copyOfRange(overrideMethodDescs, 0, off));
		return newClassByteCode.loadSelfClass();
	}

	private ClassBytecode generateAsyncTask(String name, String pkg, String methodName, String methodDesc) {

		String proxyMethodName = "super$"+methodName;
		
		String[] args = DescriptorUtil.methodDescToArgDescs(methodDesc);
		String[] consArgs = new String[args.length + 1];
		consArgs[0] = "L" + this.rawClassName + ";";
		System.arraycopy(args, 0, consArgs, 1, args.length);
		
		ClassBytecode newcls = new ClassBytecode();
		newcls.toEmptyImplClass(name, pkg, AsyncTask.class);
		ConstantPool cp = newcls.getConstantPool();
		int objInitIndex = cp.addMethod("<init>", "()V", "java/lang/Object");
		int methodIndex = cp.addMethod(proxyMethodName, methodDesc, this.rawClassName);
		

		/**
		 * add constructor <init>(consArgs)V
		 * */
		CodeAttributeWriter writer = CodeAttributeWriter.of(cp.findName("Code"));
		writer.addInstruction("aload_0")
			.addInstruction16("invokespecial", objInitIndex);
		int slot = 1;
		for(int j = 0; j < consArgs.length; j++) {
			int fieldIndex = newcls.addField("p"+j, DescriptorUtil.getDescriptionClassName(consArgs[j]), ClassBytecode.ACC_PUBLIC);
			writer.addInstruction("aload_0");
			slot = loadParamCode(writer, consArgs[j], slot);
			writer.addInstruction16("putfield",fieldIndex);
		}
		writer.addInstruction("return");

		newcls.addConstructor(consArgs, writer.toCodeAttribute());
		

		/**
		 * add method run()V { impl.super$method(); }
		 */
		CodeAttributeWriter runWriter = CodeAttributeWriter.of(cp.findName("Code"));
		for(int j = 0; j < consArgs.length; j++) {
			int fieldIndex = newcls.findField("p" + j);
			runWriter.addInstruction("aload_0")
				.addInstruction16("getfield", fieldIndex);
		}
		runWriter.addInstruction16("invokevirtual", methodIndex)
			.addInstruction("return");

		newcls.addMethod("run", new String[0], "V", runWriter.toCodeAttribute());
		
		return newcls;
	}
	
	
	public ClassBytecode generateAsyncClassCode(String pkg, String overrideMethods[], String overrideMethodDescs[]) {
		if(overrideMethods.length != overrideMethodDescs.length) {
			throw new IllegalArgumentException("the number of overrideMethods and overrideMethodDescs must equal");
		}
		
		ClassBytecode newcls = new ClassBytecode();
		newcls.toEmptyImplClass(this.classSimpleName, pkg, this.superRawClassName, false);
		ConstantPool cp = newcls.getConstantPool();

		//add field AsyncPool, impl.pool
		int poolFieldIndex = newcls.addField("pool", AsyncPool.class, ClassBytecode.ACC_PUBLIC);
		
		//add invoke method impl.pool.submit(implTask)
		int submitIndex = cp.addMethod("submit", "(" + DescriptorUtil.getClassDescription(AsyncTask.class) + ")V", DescriptorUtil.replaceDot2Slash(AsyncPool.class.getName()));
		int exMethodIndex = cp.addMethod("printStackTrace", "()V", "java/lang/Exception");
		int stackMapIndex = cp.addName("StackMapTable");
		int exClassIndex = cp.findClass("java/lang/Exception");
		
		for(int i = 0; i < overrideMethods.length; i++) {
			
			String methodName =  overrideMethods[i], methodDesc = overrideMethodDescs[i];
			String proxyMethodName = "super$"+methodName;

			String taskRawClassName = DescriptorUtil.replaceDot2Slash(taskClassName + (i + 1));
			cp.addClass(taskRawClassName);
			
			/**
			 * add method impl.super$methodName(args)V {try{super.methodName();}catch(Exception e){e.printStackTrace();}}
			 * */
			int superMethodIndex = cp.addMethod(methodName, methodDesc, this.superRawClassName);
			CodeAttributeWriter writer = CodeAttributeWriter.of(cp.findName("Code"));
			writer.addInstruction("aload_0");
			
			String[] args = DescriptorUtil.methodDescToArgDescs(methodDesc);
			int slot = 1;
			int exStart = 0, exEnd = 0, exTarget = 0, exType = exClassIndex;
			for(int j = 0; j < args.length; j++) {
				slot = loadParamCode(writer, args[j], slot);
			}
			writer.addInstruction16("invokespecial", superMethodIndex);
			exEnd = writer.currentPc();
			writer.addInstruction16("goto", 8);
			exTarget = writer.currentPc();
			if(slot > 3) {
				writer.addInstruction8("astore", slot);
			} else {
				writer.addInstruction("astore_" + slot);
			}
			if(slot > 3) {
				writer.addInstruction8("aload", slot);
				slot++;
			} else {
				writer.addInstruction("aload_" + slot);
			}
			writer.addInstruction16("invokevirtual", exMethodIndex);
			writer.addInstruction("return");
			
			CodeAttribute codeAttr = writer.toCodeAttribute();
			codeAttr.setExceptionTable(new ExceptionTable[] {new ExceptionTable(exStart, exEnd, exTarget, exType)});
			
			StackMapAttributeWriter stackMapWriter = StackMapAttributeWriter.of(stackMapIndex);
			stackMapWriter.add((new StackMapEntry(72)).addFrame(7, exClassIndex, 0));
			stackMapWriter.add((new StackMapEntry(4)));
			
			codeAttr.setAttributes(new AttributeInfo[] {stackMapWriter.toStackMapAttribute()});
			
			newcls.addMethod(proxyMethodName, args, "V", codeAttr);
			

			/**
			 * add override method impl.methodName(args)V {pool.submit(new implTask(this, args));}
			 * */

			String[] taskInitArgs = new String[args.length + 1];
			taskInitArgs[0] = "L" + this.rawClassName + ";";
			System.arraycopy(args, 0, taskInitArgs, 1, args.length);
			// add invoke new implTask(this, args);
			int taskMethodIndex = cp.addMethod("<init>", DescriptorUtil.getMethodDescription(taskInitArgs, "V"), taskRawClassName);
			
			CodeAttributeWriter overrideWriter = CodeAttributeWriter.of(cp.findName("Code"));
			overrideWriter.addInstruction16("new", cp.findClass(taskRawClassName))
				.addInstruction("dup")
				.addInstruction("aload_0");
			slot = 1;
			for(int j = 0; j < args.length; j++) {
				slot = loadParamCode(overrideWriter, args[j], slot);
			}
			overrideWriter.addInstruction16("invokespecial", taskMethodIndex);
			if(slot > 3) {
				overrideWriter.addInstruction8("astore", slot);
			} else {
				overrideWriter.addInstruction("astore_" + slot);
			}
			overrideWriter.addInstruction("aload_0")
				.addInstruction16("getfield", poolFieldIndex);
			if(slot > 3) {
				overrideWriter.addInstruction8("aload", slot);
			} else {
				overrideWriter.addInstruction("aload_" + slot);
			}
			slot++;
			overrideWriter.addInstruction16("invokevirtual", submitIndex)
				.addInstruction("return");

			newcls.addMethod(methodName, args, "V", overrideWriter.toCodeAttribute());
		}
		
		return newcls;
	}
	
	private int loadParamCode(CodeAttributeWriter writer, String arg, int slot) {
		if(arg.length() == 1) {
			byte c = arg.getBytes()[0];
			if(c == 'Z' || c == 'B' || c == 'C' || c == 'S' || c == 'I') {
				if(slot == 1) {
					writer.addInstruction("iload_1");
					slot++;
				} else if(slot == 2) {
					writer.addInstruction("iload_1");
					slot++;
				} else if(slot == 3) {
					writer.addInstruction("iload_1");
					slot++;
				} else {
					writer.addInstruction8("iload", (byte)slot);
					slot++;
				}
			} else if(c == 'J') {
				if(slot == 1) {
					writer.addInstruction("lload_1");
					slot += 2;
				} else if(slot == 2) {
					writer.addInstruction("lload_2");
					slot += 2;
				} else if(slot == 3) {
					writer.addInstruction("lload_3");
					slot += 2;
				} else {
					writer.addInstruction8("lload", slot);
					slot += 2;
				}
			} else if(c == 'F') {
				if(slot == 1) {
					writer.addInstruction("fload_1");
					slot++;
				} else if(slot == 2) {
					writer.addInstruction("fload_2");
					slot++;
				} else if(slot == 3) {
					writer.addInstruction("fload_3");
					slot++;
				} else {
					writer.addInstruction8("fload", slot);
					slot++;
				}
			} else if(c == 'D') {
				if(slot == 1) {
					writer.addInstruction("dload_1");
					slot += 2;
				} else if(slot == 2) {
					writer.addInstruction("dload_2");
					slot += 2;
				} else if(slot == 3) {
					writer.addInstruction("dload_3");
					slot += 2;
				} else  {
					writer.addInstruction8("dload", slot);
					slot += 2;
				}
			}
			throw new BytecodeException("Invalid instruction load type " + arg);
		} else {
			if(slot == 1) {
				writer.addInstruction("aload_1");
				slot++;
			} else if(slot == 2) {
				writer.addInstruction("aload_2");
				slot++;
			} else if(slot == 3) {
				writer.addInstruction("aload_3");
				slot++;
			} else {
				writer.addInstruction8("aload", slot);
				slot++;
			}
		}
		return slot;
	}
}

