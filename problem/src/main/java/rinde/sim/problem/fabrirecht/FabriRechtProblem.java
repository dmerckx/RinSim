/**
 * 
 */
package rinde.sim.problem.fabrirecht;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;

import rinde.sim.core.model.pdp.scenario.PDPScenarioEvent;
import rinde.sim.core.model.pdp.twpolicy.TardyAllowedPolicy;
import rinde.sim.core.model.road.AbstractRoadModel.RoadEvent;
import rinde.sim.core.model.road.users.MovingRoadUser;
import rinde.sim.core.model.road.MoveEvent;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.old.pdp.PDPModel;
import rinde.sim.core.old.pdp.PDPModel.PDPModelEvent;
import rinde.sim.core.old.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.simulation.Simulator;
import rinde.sim.core.simulation.Simulator.SimulatorEventType;
import rinde.sim.event.Event;
import rinde.sim.event.EventAPI;
import rinde.sim.event.EventDispatcher;
import rinde.sim.event.Listener;
import rinde.sim.scenario.ScenarioController;
import rinde.sim.scenario.TimedEvent;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public abstract class FabriRechtProblem extends ScenarioController {

	protected Simulator simulator;
	protected RoadModel roadModel;
	protected PDPModel pdpModel;
	protected ParcelAssesor parcelAssesor;
	protected final FabriRechtScenario fabriRechtScenario;
	protected final StatisticsListener statisticsListener;

	/**
	 * @param scen
	 * @param numberOfTicks
	 */
	public FabriRechtProblem(FabriRechtScenario scen) {
		super(scen, (int) (scen.timeWindow.end - scen.timeWindow.begin));
		fabriRechtScenario = scen;
		statisticsListener = new StatisticsListener();

	}

	// subclasses can override this method to add more models
	@Override
	protected Simulator createSimulator() throws Exception {
		simulator = new Simulator(1);
		roadModel = new PlaneRoadModel(fabriRechtScenario.min, fabriRechtScenario.max, false, 1.0);
		pdpModel = new PDPModel(new TardyAllowedPolicy());
		simulator.getEventAPI().addListener(statisticsListener, SimulatorEventType.values());
		roadModel.getEventAPI().addListener(statisticsListener, RoadEvent.MOVE);
		pdpModel.getEventAPI()
				.addListener(statisticsListener, PDPModelEventType.START_PICKUP, PDPModelEventType.END_PICKUP, PDPModelEventType.START_DELIVERY, PDPModelEventType.END_DELIVERY);
		simulator.register(roadModel);
		simulator.register(pdpModel);
		parcelAssesor = createParcelAssesor();
		return simulator;
	}

	@Override
	protected final boolean handleTimedEvent(TimedEvent event) {
		if (event.getEventType() == PDPScenarioEvent.ADD_PARCEL) {
			statisticsListener.addedParcels++;
			return handleAddParcel(((AddParcelEvent) event));
		} else if (event.getEventType() == PDPScenarioEvent.ADD_VEHICLE) {
			return handleAddVehicle((AddVehicleEvent) event);
		} else if (event.getEventType() == PDPScenarioEvent.ADD_DEPOT) {
			return handleAddDepot((AddDepotEvent) event);
		} else if (event.getEventType() == PDPScenarioEvent.TIME_OUT) {
			getSimulator().stop();

			final Set<FRVehicle> vehicles = roadModel.getObjectsOfType(FRVehicle.class);
			final FRDepot depot = roadModel.getObjectsOfType(FRDepot.class).iterator().next();
			int vehicleBack = 0;
			for (final FRVehicle v : vehicles) {
				if (roadModel.equalPosition(depot, v)) {
					vehicleBack++;
				}
			}

			statisticsListener.handleSimFinish(vehicleBack, vehicles.size());

			return handleTimeOut();
		}
		return false;
	}

	protected abstract ParcelAssesor createParcelAssesor();

	protected abstract boolean handleAddVehicle(AddVehicleEvent event);

	protected abstract boolean handleTimeOut();

	protected boolean handleAddParcel(AddParcelEvent event) {
		if (parcelAssesor.acceptParcel(event.parcelDTO)) {
			statisticsListener.acceptedParcels++;
			return getSimulator().register(new FRParcel(event.parcelDTO));
		}
		return true;
	}

	protected boolean handleAddDepot(AddDepotEvent event) {
		return getSimulator().register(new FRDepot(event.position));
	}

	public StatisticsDTO getStatistics() {
		return statisticsListener.getDTO();
	}

	public static class StatisticsDTO implements Serializable {
		private static final long serialVersionUID = 1968951252238291733L;
		public final double totalDistance;
		public final int totalPickups;
		public final int totalDeliveries;
		public final int totalParcels;
		public final int acceptedParcels;
		public final long pickupTardiness;
		public final long deliveryTardiness;
		public final long computationTime;
		public final long simulationTime;
		public final boolean simFinish;
		public final int vehiclesAtDepot;
		public final int totalVehicles;

		public StatisticsDTO(double dist, int pick, int del, int parc, int accP, long pickTar, long delTar, long compT,
				long simT, boolean finish, int atDepot, int total) {
			totalDistance = dist;
			totalPickups = pick;
			totalDeliveries = del;
			totalParcels = parc;
			acceptedParcels = accP;
			pickupTardiness = pickTar;
			deliveryTardiness = delTar;
			computationTime = compT;
			simulationTime = simT;
			simFinish = finish;
			vehiclesAtDepot = atDepot;
			totalVehicles = total;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("\t\t\t = Statistics = \ncomputation time:\t\t").append(computationTime);
			sb.append("\nsimulation time:\t\t").append(simulationTime);
			sb.append("\ntotal traveled distance:\t").append(totalDistance);
			sb.append("\naccepted parcels:\t\t").append(acceptedParcels).append(" / ").append(totalParcels);
			sb.append("\t").append(Math.round(((double) acceptedParcels / totalParcels) * 100d)).append("%");
			sb.append("\npickups:\t\t\t").append(totalPickups).append(" / ").append(acceptedParcels);
			sb.append("\t").append(Math.round(((double) totalPickups / acceptedParcels) * 100d)).append("%");
			sb.append("\ndeliveries:\t\t\t").append(totalDeliveries).append(" / ").append(acceptedParcels);
			sb.append("\t").append(Math.round(((double) totalDeliveries / acceptedParcels) * 100d)).append("%");
			sb.append("\npickup tardiness:\t\t").append(pickupTardiness);
			sb.append("\ndelivery tardiness:\t\t").append(deliveryTardiness);
			return sb.toString();
		}
	}

	public enum StatisticsEventType {
		PICKUP_TARDINESS, DELIVERY_TARDINESS;
	}

	public class StatisticsListener implements Listener {

		protected EventDispatcher eventDispatcher;

		protected int acceptedParcels;
		protected final Map<MovingRoadUser, Double> distanceMap;
		protected double totalDistance;
		protected int totalPickups;
		protected int totalDeliveries;
		protected long pickupTardiness;
		protected long deliveryTardiness;
		protected int addedParcels;
		protected long startTimeReal;
		protected long startTimeSim;
		protected long computationTime;
		protected long simulationTime;
		protected boolean simFinish;
		protected int vehiclesAtDepot;
		protected int totalVehicles;

		public StatisticsListener() {
			distanceMap = newLinkedHashMap();
			totalDistance = 0d;
			totalPickups = 0;
			totalDeliveries = 0;
			addedParcels = 0;
			acceptedParcels = 0;
			simFinish = false;
			eventDispatcher = new EventDispatcher(StatisticsEventType.values());
		}

		@Override
		public void handleEvent(Event e) {
			if (e.getEventType() == SimulatorEventType.STARTED) {
				startTimeReal = System.currentTimeMillis();
				startTimeSim = getSimulator().getCurrentTime();
			} else if (e.getEventType() == SimulatorEventType.STOPPED) {
				computationTime = System.currentTimeMillis() - startTimeReal;
				simulationTime = getSimulator().getCurrentTime() - startTimeSim;
			} else if (e.getEventType() == RoadEvent.MOVE) {
				final MoveEvent me = ((MoveEvent) e);
				increment(me.roadUser, me.pathProgress.distance);
				totalDistance += me.pathProgress.distance;
			} else if (e.getEventType() == PDPModelEventType.START_PICKUP) {

				final PDPModelEvent pme = (PDPModelEvent) e;
				final long latestBeginTime = pme.parcel.getPickupTimeWindow().end - pme.parcel.getPickupDuration();
				if (pme.time > latestBeginTime) {
					// System.out.println(pme.time);
					// System.out.println("pickup tardiness for: " +
					// ReflectionToStringBuilder.toString(pme.parcel));
					// System.out.println(pme.time - latestBeginTime);
					pickupTardiness += pme.time - latestBeginTime;
					eventDispatcher.dispatchEvent(new Event(StatisticsEventType.PICKUP_TARDINESS, this));

					// throw new RuntimeException();
				}

			} else if (e.getEventType() == PDPModelEventType.END_PICKUP) {
				totalPickups++;
			} else if (e.getEventType() == PDPModelEventType.START_DELIVERY) {
				final PDPModelEvent pme = (PDPModelEvent) e;
				final long latestBeginTime = pme.parcel.getDeliveryTimeWindow().end - pme.parcel.getDeliveryDuration();
				if (pme.time > latestBeginTime) {
					// System.out.println("delivery tardiness for: " +
					// ReflectionToStringBuilder.toString(pme.parcel));
					deliveryTardiness += pme.time - latestBeginTime;
					eventDispatcher.dispatchEvent(new Event(StatisticsEventType.DELIVERY_TARDINESS, this));
				}
			} else if (e.getEventType() == PDPModelEventType.END_DELIVERY) {
				totalDeliveries++;
			}
		}

		void handleSimFinish(int numVehiclesBack, int totalNumVehicles) {
			simFinish = true;
			vehiclesAtDepot = numVehiclesBack;
			totalVehicles = totalNumVehicles;
		}

		protected void increment(MovingRoadUser mru, double num) {
			if (!distanceMap.containsKey(mru)) {
				distanceMap.put(mru, num);
			} else {
				distanceMap.put(mru, distanceMap.get(mru) + num);
			}
		}

		public double getTotalTraveledDistance() {
			return totalDistance;
		}

		public int getTotalPickups() {
			return totalPickups;
		}

		public int getTotalDeliveries() {
			return totalDeliveries;
		}

		public long getComputationTime() {
			return computationTime;
		}

		public EventAPI getEventAPI() {
			return eventDispatcher.getEventAPI();
		}

		public StatisticsDTO getDTO() {
			return new StatisticsDTO(totalDistance, totalPickups, totalDeliveries, addedParcels, acceptedParcels,
					pickupTardiness, deliveryTardiness, computationTime, simulationTime, simFinish, vehiclesAtDepot,
					totalVehicles);
		}
	}

}
