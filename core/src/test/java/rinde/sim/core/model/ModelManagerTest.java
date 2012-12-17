package rinde.sim.core.model;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static rinde.sim.core.model.DebugModel.Action.ALLOW;
import static rinde.sim.core.model.DebugModel.Action.REJECT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rinde.sim.core.dummies.DummyAbstrModel;
import rinde.sim.core.dummies.DummyModel;
import rinde.sim.core.dummies.DummyUser;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.core.simulation.UserInit;
import rinde.sim.core.model.User;

import com.google.common.collect.Lists;

public class ModelManagerTest {

	protected ModelManager manager;
	protected DummyUser user;
	
	@Before
	public void setUp() {
		manager = new ModelManager();
		user = new DummyUser();
	}

	@Test(expected = IllegalStateException.class)
	public void notConfigured() {
		manager.register(user, new Data(){});
	}

	@Test
	public void addOtherFooModel() {
		FooModel model = new FooModel();
		manager.add(model);
		manager.configure();
		manager.register(new Foo(), new FooData());
		manager.register(new Bar(), new BarData());
		assertEquals(1, model.calledRegister);
		assertEquals(1, model.calledTypes);
	}

	@Test
	public void addWhenTwoModels() {
		FooModel model = new FooModel();
		BarModel model2 = new BarModel();
		
		manager.add(model);
		manager.add(model2);
		manager.configure();
		
		manager.register(new Foo(), new FooData());
		manager.register(new Bar(), new BarData());
		manager.register(new Foo(), new FooData());
		
		assertEquals(2, model.calledRegister);
		assertEquals(1, model.calledTypes);
		assertEquals(1, model2.calledRegister);

		assertArrayEquals(new Model<?,?>[] { model, model2 }, manager.getModels().toArray(new Model<?,?>[2]));
	}

	@Test
	public void addDuplicateModel() {
		FooModel model = new FooModel();
		assertTrue(manager.add(model));
		assertFalse(manager.add(model));
	}

	@Test(expected = AssertionError.class)
	public void addNull() {
		manager.add(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addFaultyModel() {
		ModelA model = new ModelA();
		model.setSupportedType(null);
		manager.add(model);
	}

	@Test(expected = AssertionError.class)
	public void registerNull() {
		manager.register(null, null);
	}
	
	@Test(expected = AssertionError.class)
    public void registerNull2() {
        manager.register(new User<Data>() {}, null);
    }
	
	@Test(expected = AssertionError.class)
    public void registerNull3() {
        manager.register(null, new Data() {});
    }

	@Test(expected = IllegalStateException.class)
	public void addModelTooLate() {
		manager.configure();
		manager.add(new FooModel());
	}

	@Test(expected = RuntimeException.class)
	public void registerWithBrokenModel() {
		manager.add(new BrokenModel());
		manager.configure();
		manager.register(user, new Data() {});
	}

	@Test
	public void registerWithoutModels() {
		manager.configure();
		assertEquals(0, manager.getModels().size());
		manager.register(new Foo(), new FooData());
        assertEquals(0, manager.getModels().size());
		manager.unregister(new Foo());
        assertEquals(0, manager.getModels().size());
	}

	@Test(expected = AssertionError.class)
	public void unregisterNull() {
		manager.unregister(null);
	}

	@Test(expected = IllegalStateException.class)
	public void unregisterWhenNotConfigured() {
		manager.unregister(new Foo());
	}

	@Test
	public void unregister() {
		manager.add(new FooModel());
		manager.add(new BarModel());
		manager.configure();
		manager.unregister(new Foo());
	}

	@Test(expected = RuntimeException.class)
	public void unregisterWithBrokenModel() {
		manager.add(new BrokenModel());
		manager.configure();
		manager.unregister(new DummyUser());
	}

	public void unregisterRegistered() {
		FooModel model = new FooModel();
		BarModel model2 = new BarModel();
		manager.add(model);
		manager.add(model2);
		manager.configure();

		final Foo foo = new Foo();
		final Bar bar = new Bar();

		manager.register(foo, new FooData());
		manager.register(bar, new BarData());
		manager.unregister(foo);

		assertEquals(1, model.calledRegister);
        assertEquals(1, model.calledUnregister);
		assertEquals(1, model2.calledRegister);
        assertEquals(0, model2.calledUnregister);
	}

	@Test
	public void manyModelsTest() {
		ModelA mA = new ModelA();
		ModelAA mAA = new ModelAA();
		ModelB mB = new ModelB();
		ModelB mB2 = new ModelB();
		ModelBB mBB = new ModelBB();
		ModelBBB mBBB = new ModelBBB();
		SpecialModelB mSB = new SpecialModelB();
		ModelC mC = new ModelC();

		manager.add(mA);
		manager.add(mAA);
		manager.add(mB);
		manager.add(mB2);
		manager.add(mBB);
		manager.add(mBBB);
		manager.add(mSB);
		manager.add(mC);

		manager.configure();

		ObjectA a1 = new ObjectA();
		manager.register(a1, new FooData());
		assertEquals(asList(a1), mA.getRegisteredElements());
		assertEquals(asList(a1), mAA.getRegisteredElements());

		mA.setRegisterAction(REJECT);
		ObjectA a2 = new ObjectA();
		manager.register(a2, new FooData());
		assertEquals(asList(a1, a2), mA.getRegisteredElements());
		assertEquals(asList(a1, a2), mAA.getRegisteredElements());

		mAA.setRegisterAction(REJECT);
		ObjectA a3 = new ObjectA();
		manager.register(a3, new FooData());
		assertEquals(asList(a1, a2, a3), mA.getRegisteredElements());
		assertEquals(asList(a1, a2, a3), mAA.getRegisteredElements());

		mA.setRegisterAction(ALLOW);
		mAA.setRegisterAction(ALLOW);
		manager.register(a1, new FooData());// allow duplicates
		assertEquals(asList(a1, a2, a3, a1), mA.getRegisteredElements());
		assertEquals(asList(a1, a2, a3, a1), mAA.getRegisteredElements());

		ObjectB b1 = new ObjectB();
		manager.register(b1, new FooData());
		assertEquals(asList(b1), mB.getRegisteredElements());
		assertEquals(asList(b1), mB2.getRegisteredElements());
		assertEquals(asList(b1), mBB.getRegisteredElements());
		assertEquals(asList(b1), mBBB.getRegisteredElements());
		assertEquals(asList(), mSB.getRegisteredElements());

		// subclass of B is registerd in all general models and its subclass
		// model
		SpecialB s1 = new SpecialB();
		manager.register(s1, new FooData());
		assertEquals(asList(b1, s1), mB.getRegisteredElements());
		assertEquals(asList(b1, s1), mB2.getRegisteredElements());
		assertEquals(asList(b1, s1), mBB.getRegisteredElements());
		assertEquals(asList(b1, s1), mBBB.getRegisteredElements());
		assertEquals(asList(s1), mSB.getRegisteredElements());

		assertTrue(mC.getRegisteredElements().isEmpty());

		// unregister not registered object
		ObjectA a4 = new ObjectA();
		manager.unregister(a4);
		assertEquals(asList(a4), mA.getUnregisteredElements());
		assertEquals(asList(a4), mAA.getUnregisteredElements());

		// try again, this time with models rejecting unregister
		mA.setUnregisterAction(REJECT);
		mAA.setUnregisterAction(REJECT);
		manager.unregister(a4);
		assertEquals(asList(a4, a4), mA.getUnregisteredElements());
		assertEquals(asList(a4, a4), mAA.getUnregisteredElements());

		manager.unregister(b1);
		assertEquals(asList(b1), mB.getUnregisteredElements());
		assertEquals(asList(b1), mB2.getUnregisteredElements());
		assertEquals(asList(b1), mBB.getUnregisteredElements());
		assertEquals(asList(b1), mBBB.getUnregisteredElements());
		assertEquals(asList(), mSB.getUnregisteredElements());

		manager.unregister(s1);
		assertEquals(asList(b1, s1), mB.getUnregisteredElements());
		assertEquals(asList(b1, s1), mB2.getUnregisteredElements());
		assertEquals(asList(b1, s1), mBB.getUnregisteredElements());
		assertEquals(asList(b1, s1), mBBB.getUnregisteredElements());
		assertEquals(asList(s1), mSB.getUnregisteredElements());

	}
}

class Foo implements User<FooData>{
    
}

class FooData implements Data{
    
}

class Bar implements User<BarData>{

}

class BarData implements Data{
    
}

class BrokenModel extends DummyModel {
	
    @Override
	public List<UserInit<?>> register(DummyUser user, Data data) {
		throw new RuntimeException("intended failure");
	}

	@Override
	public List<User<?>> unregister(DummyUser obj) {
		throw new RuntimeException("intended failure");
	}
}

class FooModel extends DummyAbstrModel<FooData, Foo>{
    int calledTypes;
	int calledRegister;
	int calledUnregister;

	@Override
	public List<UserInit<?>> register(Foo element, FooData data) {
		calledRegister += 1;
		
		return Lists.newArrayList();
	}

	@Override
	public Class<Foo> getSupportedType() {
		calledTypes += 1;
		return Foo.class;
	}

	@Override
	public List<User<?>> unregister(Foo element) {
		calledUnregister += 1;
		
        return Lists.newArrayList();
	}
}

class BarModel extends DummyAbstrModel<BarData, Bar> {
    int calledTypes;
    int calledRegister;
    int calledUnregister;


    @Override
    public List<UserInit<?>> register(Bar element, BarData data) {
        calledRegister += 1;
        
        return Lists.newArrayList();
    }


    @Override
    public List<User<?>> unregister(Bar element) {
        calledUnregister += 1;
        
        return Lists.newArrayList();
    }

    @Override
    public Class<Bar> getSupportedType() {
        calledTypes += 1;
        return Bar.class;
    }
}

class ObjectA extends Foo{}

class ObjectB extends Foo{}

class SpecialB extends ObjectB {}

class ObjectC extends Foo{}

class ModelA extends DebugModel<ObjectA> {
	ModelA() {
		super(ObjectA.class);
	}
}

class ModelAA extends DebugModel<ObjectA> {
	ModelAA() {
		super(ObjectA.class);
	}
}

class ModelB extends DebugModel<ObjectB> {
	ModelB() {
		super(ObjectB.class);
	}
}

class ModelBB extends DebugModel<ObjectB> {
	ModelBB() {
		super(ObjectB.class);
	}
}

class ModelBBB extends DebugModel<ObjectB> {
	ModelBBB() {
		super(ObjectB.class);
	}
}

class SpecialModelB extends DebugModel<SpecialB> {
	SpecialModelB() {
		super(SpecialB.class);
	}
}

class ModelC extends DebugModel<ObjectC> {
	ModelC() {
		super(ObjectC.class);
	}
}

class DebugModel<T extends User<?>> implements Model<Data, T> {

	enum Action {
		ALLOW, REJECT, FAIL
	}

	private Action registerAction;
	private Action unregisterAction;
	private Class<T> supportedType;
	private final List<T> registeredElements;
	private final List<T> unregisteredElements;

	public DebugModel(Class<T> type) {
		supportedType = type;
		registeredElements = new ArrayList<T>();
		unregisteredElements = new ArrayList<T>();
		setRegisterAction(ALLOW);
		setUnregisterAction(ALLOW);
	}

	public void setRegisterAction(Action a) {
		registerAction = a;
	}

	public void setUnregisterAction(Action a) {
		unregisterAction = a;
	}

	public void setSupportedType(Class<T> type) {
		supportedType = type;
	}

	@Override
	public List<UserInit<?>> register(T element, Data data) {
		registeredElements.add(element);
		actionResponse(registerAction);
		
		return Lists.newArrayList();
	}

	@Override
	public List<User<?>> unregister(T element) {
		unregisteredElements.add(element);
		actionResponse(unregisterAction);
        return Lists.newArrayList();
	}

	public List<T> getRegisteredElements() {
		return Collections.unmodifiableList(registeredElements);
	}

	public List<T> getUnregisteredElements() {
		return Collections.unmodifiableList(unregisteredElements);
	}

	private boolean actionResponse(Action a) {
		switch (a) {
		case ALLOW:
			return true;
		case REJECT:
			return false;
		case FAIL:
			throw new RuntimeException("this is an intentional failure");
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public Class<T> getSupportedType() {
		return supportedType;
	}

    @Override
    public void tick(TimeInterval time) {
        
    }
}