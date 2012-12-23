package rinde.sim.ui;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelReceiver;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.TickListener;
import rinde.sim.core.simulation.TimeInterval;
import rinde.sim.ui.renderers.Renderer;
import rinde.sim.ui.renderers.ViewPort;
import rinde.sim.ui.renderers.ViewRect;
import rinde.sim.util.TimeFormatter;

/**
 * Simulation viewer.
 * 
 * @author Bartosz Michalik <bartosz.michalik@cs.kuleuven.be>
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SimulationViewer extends Composite implements TickListener, ControlListener, PaintListener,
		SelectionListener {

	public static final String COLOR_WHITE = "white";
	public static final String COLOR_GREEN = "green";
	public static final String COLOR_BLACK = "black";

	public static final String DEFAULT_COLOR = "default_color";

	public static final String ICO_PKG = "package";

	protected Canvas canvas;
	protected org.eclipse.swt.graphics.Point origin;
	protected org.eclipse.swt.graphics.Point size;

	protected Image image;

	private final Simulator simulator;
	private Map<String, Color> colorRegistry;

	/** model renderers */
	protected final Renderer[] renderers;
	private Label timeLabel;
	private ScrollBar hBar;
	private ScrollBar vBar;
	// rendering frequency related
	private int speedUp;
	private long lastRefresh;

	boolean firstTime = true;

	ViewRect viewRect;

	// private double minX;
	// private double minY;
	protected double m;// multiplier
	// // private double deltaX;
	// private double deltaY;
	private int zoomRatio;

	private final Display display;

	protected final Set<ModelReceiver> modelRenderers;

	public SimulationViewer(Shell shell, final Simulator sim, int speedUp, Renderer... renderers) {
		super(shell, SWT.NONE);

		modelRenderers = newLinkedHashSet();
		for (final Renderer r : renderers) {
			if (r instanceof ModelReceiver) {
				modelRenderers.add((ModelReceiver) r);
			}
		}
		simulator = sim;
		simulator.registerTickListener(this);

		this.renderers = renderers;
		this.speedUp = speedUp;
		shell.setLayout(new FillLayout());
		setLayout(new FillLayout());

		display = shell.getDisplay();

		createMenu(shell);
		shell.addListener(SWT.Close, new Listener() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void handleEvent(Event event) {
				simulator.stop();
				while (simulator.isPlaying()) {}

				if (!display.isDisposed()) {
					display.dispose();
				}
			}
		});

		createContent();
	}

	protected void configureModelRenderers() {
		for (final ModelReceiver mr : modelRenderers) {
			mr.registerModelProvider(simulator.getModelProvider());
		}
	}

	// @SuppressWarnings("unchecked")
	// protected <T extends Model<?>> void registerModel(T model) {
	// final Set<Class<?>> rendererSupportedTypes = renderRegistry.keySet();
	// for (final Class<?> rendererSupportedType : rendererSupportedTypes) {
	// if (rendererSupportedType.isAssignableFrom(model.getClass())) {
	// final Collection<ModelRenderer<?>> assignableModels =
	// renderRegistry.get(rendererSupportedType);
	// for (final ModelRenderer<?> modelRenderer : assignableModels) {
	// ((ModelRenderer<T>) modelRenderer).register(model);
	// }
	// }
	// }
	// }

	/**
	 * Configure shell
	 */
	protected void createContent() {
		initColors();
		canvas = new Canvas(this, SWT.DOUBLE_BUFFERED | SWT.NONE | SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL | SWT.H_SCROLL);
		canvas.setBackground(colorRegistry.get(COLOR_WHITE));
		origin = new org.eclipse.swt.graphics.Point(0, 0);
		size = new org.eclipse.swt.graphics.Point(800, 500);
		canvas.addPaintListener(this);
		canvas.addControlListener(this);
		// canvas.redraw();
		this.layout();

		timeLabel = new Label(canvas, SWT.NONE);
		timeLabel.setText("hello world");
		timeLabel.setBounds(20, 20, 200, 20);
		timeLabel.setBackground(colorRegistry.get(COLOR_WHITE));

		hBar = canvas.getHorizontalBar();
		hBar.addSelectionListener(this);
		vBar = canvas.getVerticalBar();
		vBar.addSelectionListener(this);
	}

	/**
	 * Initializes color registry and passes it to all renderers
	 */
	protected void initColors() {
		assert getDisplay() != null : "should be called after display is initialized";
		assert renderers != null : "should be called after renderers are initialized";
		colorRegistry = newHashMap();
		colorRegistry.put(COLOR_WHITE, new Color(getDisplay(), new RGB(0xFF, 0xFF, 0xFF)));
		colorRegistry.put(COLOR_BLACK, new Color(getDisplay(), new RGB(0x00, 0x00, 0x00)));
		colorRegistry.put(COLOR_GREEN, new Color(getDisplay(), new RGB(0x00, 0xFF, 0x00)));
	}

	@SuppressWarnings("unused")
	protected void createMenu(Shell shell) {
		final Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);

		final MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
		fileItem.setText("Control");

		final Menu submenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(submenu);

		// play switch
		final MenuItem item = new MenuItem(submenu, SWT.PUSH);
		item.setText("&Play");
		item.setAccelerator(SWT.MOD1 + 'P');
		item.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				onToglePlay((MenuItem) e.widget);
			}
		});

		new MenuItem(submenu, SWT.SEPARATOR);
		// step execution switch
		final MenuItem nextItem = new MenuItem(submenu, SWT.PUSH);
		nextItem.setText("Next tick");
		nextItem.setAccelerator(SWT.MOD1 + ']');
		nextItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				onTick((MenuItem) e.widget);
			}
		});

		// view options

		final MenuItem viewItem = new MenuItem(bar, SWT.CASCADE);
		viewItem.setText("View");

		final Menu viewMenu = new Menu(shell, SWT.DROP_DOWN);
		viewItem.setMenu(viewMenu);

		// zooming
		final MenuItem zoomInItem = new MenuItem(viewMenu, SWT.PUSH);
		zoomInItem.setText("Zoom in");
		zoomInItem.setAccelerator(SWT.MOD1 + '+');
		zoomInItem.setData("in");

		final MenuItem zoomOutItem = new MenuItem(viewMenu, SWT.PUSH);
		zoomOutItem.setText("Zoom out");
		zoomOutItem.setAccelerator(SWT.MOD1 + '-');
		zoomOutItem.setData("out");

		final Listener zoomingListener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				onZooming((MenuItem) e.widget);
			}
		};
		zoomInItem.addListener(SWT.Selection, zoomingListener);
		zoomOutItem.addListener(SWT.Selection, zoomingListener);

		// speedUp

		final Listener speedUpListener = new Listener() {

			@Override
			public void handleEvent(Event e) {
				onSpeedChange((MenuItem) e.widget);
			}
		};

		final MenuItem increaseSpeedItem = new MenuItem(submenu, SWT.PUSH);
		increaseSpeedItem.setAccelerator(SWT.MOD1 + '.');
		increaseSpeedItem.setText("Speed up");
		increaseSpeedItem.setData(">");
		increaseSpeedItem.addListener(SWT.Selection, speedUpListener);
		//
		final MenuItem decreaseSpeed = new MenuItem(submenu, SWT.PUSH);
		decreaseSpeed.setAccelerator(SWT.MOD1 + ',');
		decreaseSpeed.setText("Slow down");
		decreaseSpeed.setData("<");
		decreaseSpeed.addListener(SWT.Selection, speedUpListener);

	}

	/**
	 * Default implementation of the play/pause action. Can be overridden if
	 * needed.
	 * 
	 * @param source
	 */
	protected void onToglePlay(MenuItem source) {
		if (simulator.isPlaying()) {
			source.setText("&Play");
		} else {
			source.setText("&Pause");
		}
		new Thread() {
			@Override
			public void run() {
				simulator.togglePlayPause();
			}
		}.start();
	}

	/**
	 * Default implementation of step execution action. Can be overridden if
	 * needed.
	 * 
	 * @param source
	 */
	protected void onTick(MenuItem source) {
		if (simulator.isPlaying()) {
			simulator.stop();
		}
		simulator.advanceTick();
	}

	protected void onZooming(MenuItem source) {
		if (source.getData().equals("in")) {
			if (zoomRatio == 16) {
				return;
			}
			origin.x -= m * viewRect.width / 2;
			origin.y -= m * viewRect.height / 2;
			m *= 2;
			zoomRatio <<= 1;
		} else {
			if (zoomRatio < 2) {
				return;
			}
			m /= 2;
			origin.x += m * viewRect.width / 2;
			origin.y += m * viewRect.height / 2;
			zoomRatio >>= 1;
		}
		if (image != null) {
			image.dispose();
		}
		image = null;// this forces a redraw
		canvas.redraw();
	}

	protected void onSpeedChange(MenuItem source) {
		if (">".equals(source.getData())) {
			speedUp <<= 1;
		} else {
			if (speedUp > 1) {
				speedUp >>= 1;
			}
		}
	}
	
	public Image drawRoads() {
		size = new org.eclipse.swt.graphics.Point((int) (m * viewRect.width), (int) (m * viewRect.height));
		final Image img = new Image(getDisplay(), size.x + 10, size.y + 10);
		final GC gc = new GC(img);

		for (final Renderer r : renderers) {
			r.renderStatic(gc, new ViewPort(new Point(0, 0)/*
															 * new
															 * Point(origin.x,
															 * origin.y)
															 */, viewRect, m, colorRegistry));
		}

		//
		// Graph<? extends EdgeData> graph = roadModel.getGraph();
		//
		// for (Connection<? extends EdgeData> e : graph.getConnections()) {
		// int x1 = (int) ((e.from.x - minX) * m);
		// int y1 = (int) ((e.from.y - minY) * m);
		// // gc.setForeground(colorRegistry.get(COLOR_GREEN));
		// // gc.drawOval(x1 - 2, y1 - 2, 4, 4);
		//
		// int x2 = (int) ((e.to.x - minX) * m);
		// int y2 = (int) ((e.to.y - minY) * m);
		// gc.setForeground(colorRegistry.get(COLOR_BLACK));
		// gc.drawLine(x1, y1, x2, y2);
		//
		// // gc.setBackground(colorRegistry.get(COLOR_WHITE));
		// // gc.drawText(Math.round(e.edgeData.getLength() * 10.0) / 10.0 +
		// // "m", (x1 + x2) / 2, (y1 + y2) / 2, false);
		// }
		gc.dispose();

		return img;
	}

	@Override
	public void paintControl(PaintEvent e) {
		final GC gc = e.gc;

		if (firstTime) {
			configureModelRenderers();
			calculateSizes();
			firstTime = false;
		}

		if (image == null) {
			image = drawRoads();
			updateScrollbars(false);
		}

		gc.drawImage(image, origin.x, origin.y);

		final Rectangle rect = image.getBounds();
		final Rectangle client = canvas.getClientArea();
		final int marginWidth = client.width - rect.width;
		if (marginWidth > 0) {
			gc.fillRectangle(rect.width, 0, marginWidth, client.height);
		}
		final int marginHeight = client.height - rect.height;
		if (marginHeight > 0) {
			gc.fillRectangle(0, rect.height, client.width, marginHeight);
		}

		for (final Renderer renderer : renderers) {
			renderer.renderDynamic(gc, new ViewPort(new Point(origin.x, origin.y), viewRect, m, colorRegistry), simulator
					.getCurrentTime());
			// renderer.render(gc, origin.x, origin.y, minX, minY, m);
		}
	}

	protected void updateScrollbars(boolean adaptToScrollbar) {
		final Rectangle rect = image.getBounds();
		final Rectangle client = canvas.getClientArea();
		hBar.setMaximum(rect.width);
		vBar.setMaximum(rect.height);
		hBar.setThumb(Math.min(rect.width, client.width));
		vBar.setThumb(Math.min(rect.height, client.height));
		final int hPage = rect.width - client.width;
		final int vPage = rect.height - client.height;
		int hSelection = hBar.getSelection();
		int vSelection = vBar.getSelection();
		if (adaptToScrollbar) {
			if (hSelection >= hPage) {
				if (hPage <= 0) {
					hSelection = 0;
				}
				origin.x = -hSelection;
			}
			if (vSelection >= vPage) {
				if (vPage <= 0) {
					vSelection = 0;
				}
				origin.y = -vSelection;
			}
		} else {
			hBar.setSelection(-origin.x);
			vBar.setSelection(-origin.y);
		}
	}

	private void calculateSizes() {
		System.out.println("SIM IS CONFIGURED: " + simulator.isConfigured());
		if (!simulator.isConfigured()) {
			return;
		}

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		boolean isDefined = false;
		for (final Renderer r : renderers) {
			final ViewRect rect = r.getViewRect();
			if (rect != null) {
				minX = Math.min(minX, rect.min.x);
				maxX = Math.max(maxX, rect.max.x);
				minY = Math.min(minY, rect.min.y);
				maxY = Math.max(maxY, rect.max.y);
				isDefined = true;
			}
		}

		checkState(isDefined, "none of the available renderers implements getViewRect(), known renderers: "
				+ Arrays.toString(renderers));

		viewRect = new ViewRect(new Point(minX, minY), new Point(maxX, maxY));

		// deltaX = maxX - minX;
		// deltaY = maxY - minY;

		// System.out.println(deltaX + " " + deltaY);

		final Rectangle area = canvas.getClientArea();
		if (viewRect.width > viewRect.height) {
			m = area.width / viewRect.width;
		} else {
			m = area.height / viewRect.height;
		}
		zoomRatio = 1;

		// m *= 2;

	}

	@Override
	public void controlMoved(ControlEvent e) {
		// not needed
	}

	@Override
	public void controlResized(ControlEvent e) {
		if (image != null) {
			updateScrollbars(true);
			canvas.redraw();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == vBar) {
			final int vSelection = vBar.getSelection();
			final int destY = -vSelection - origin.y;
			final Rectangle rect = image.getBounds();
			canvas.scroll(0, destY, 0, 0, rect.width, rect.height, false);
			origin.y = -vSelection;
		} else {
			final int hSelection = hBar.getSelection();
			final int destX = -hSelection - origin.x;
			final Rectangle rect = image.getBounds();
			canvas.scroll(destX, 0, 0, 0, rect.width, rect.height, false);
			origin.x = -hSelection;
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// not needed

	}

	@Override
	public void tick(TimeInterval timeLapse) {
		if (simulator.isPlaying() && lastRefresh + timeLapse.getTimeStep() * speedUp > timeLapse.getStartTime()) {
			return;
		}
		lastRefresh = timeLapse.getStartTime();
		try {
			Thread.sleep(30);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (display.isDisposed()) {
			return;
		}
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!canvas.isDisposed()) {
					if (simulator.getTimeStep() > 500) {
						timeLabel.setText(TimeFormatter.format(simulator.getCurrentTime()));
					} else {
						timeLabel.setText("" + simulator.getCurrentTime());
					}
					canvas.redraw();
				}
			}
		});
	}
}
