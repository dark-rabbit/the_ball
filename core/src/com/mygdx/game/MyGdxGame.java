package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import sun.print.BackgroundLookupListener;

public class MyGdxGame extends ApplicationAdapter {

	private final float PPM = 100;
	private final float RADIUS_RATIO = 10;
	private float width;
	private float height;
	private float radius;
	private boolean isBallColliding = false;
	private Vector2 collidePoint;

	private OrthographicCamera hudCamera;
	private OrthographicCamera b2Camera;
	private ShapeRenderer shapeRenderer;
	private World world;
	private Body ball;
	private Body topLimit;
	private Body bottomLimit;
	private Body leftLimit;
	private Body rightLimit;
	private RayHandler rayHandler;
	private PointLight pointLightBall;
	private PointLight pointLightTop;
	private PointLight pointLightBottom;
	private PointLight pointLightLeft;
	private PointLight pointLightRight;
	
	@Override
	public void create () {
		
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		radius = width / RADIUS_RATIO;
		
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, width, height);
		b2Camera = new OrthographicCamera();
		b2Camera.setToOrtho(false, width/PPM, height/PPM);

		shapeRenderer = new ShapeRenderer();

		// world definition
		world = new World(new Vector2(0, 0),  true);
		
		// body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(width/PPM/2, height/PPM/2);
		ball = world.createBody(bodyDef);
		
		// shape definition
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(radius/PPM);
		
		// fixture definition
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circleShape;
		fixtureDef.density = 0.5f;
		fixtureDef.restitution = 0.8f;
		Fixture fixture = ball.createFixture(fixtureDef);
		
		//
		// Limits
		//
		bodyDef.type = BodyDef.BodyType.StaticBody;
		PolygonShape boxShape = new PolygonShape();
		
		// vertical limits
		boxShape.setAsBox(width/PPM, 0);
		fixtureDef.shape = boxShape;
		fixtureDef.restitution = 0;

		// top
		bodyDef.position.set(width/PPM/2, height/PPM);
		topLimit = world.createBody(bodyDef);
		fixture = topLimit.createFixture(fixtureDef);

		// bottom
		bodyDef.position.set(width/PPM/2, 0);
		bottomLimit = world.createBody(bodyDef);
		fixture = bottomLimit.createFixture(fixtureDef);
		
		// horizontal limits
		boxShape.setAsBox(0, height/PPM);
		fixtureDef.shape = boxShape;

		// left
		bodyDef.position.set(0, height/PPM/2);
		leftLimit = world.createBody(bodyDef);
		fixture = leftLimit.createFixture(fixtureDef);

		// right
		bodyDef.position.set(width/PPM, height/PPM/2);
		rightLimit = world.createBody(bodyDef);
		fixture = rightLimit.createFixture(fixtureDef);

		//
		// lights
		//
		rayHandler = new RayHandler(world);
		rayHandler.setShadows(true);
		rayHandler.setCombinedMatrix(b2Camera);
		pointLightBall = new PointLight(rayHandler, 1000, Color.WHITE, circleShape.getRadius()*2, ball.getPosition().x, ball.getPosition().y);
		pointLightTop = new PointLight(rayHandler, 1000, Color.CHARTREUSE, 0, width/PPM/2, height/PPM);
		pointLightBottom = new PointLight(rayHandler, 1000, Color.CORAL, 0, width/PPM/2, 0);
		pointLightLeft = new PointLight(rayHandler, 1000, Color.CYAN, 0, 0, height/PPM/2);
		pointLightRight = new PointLight(rayHandler, 1000, Color.FIREBRICK, 0, width/PPM, height/PPM/2);

		
		//
		// collisions
		//
		world.setContactListener(new ContactListener () {
			
			@Override
			public void beginContact (Contact contact) {

				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();
				if (bodyA.equals(ball) || bodyB.equals(ball)) {
					isBallColliding = true;
					Gdx.input.vibrate(50);
					collidePoint = contact.getWorldManifold().getPoints()[0];
					if (bodyA.equals(topLimit) || bodyB.equals(topLimit)) {
						pointLightTop.setPosition(collidePoint.x, height/PPM);
						pointLightTop.setDistance(height*2/PPM);
						pointLightTop.setColor(randomColor());
					} else if (bodyA.equals(bottomLimit) || bodyB.equals(bottomLimit)) {
						pointLightBottom.setPosition(collidePoint.x, 0);
						pointLightBottom.setDistance(height*2/PPM);
						pointLightBottom.setColor(randomColor());
					} else if (bodyA.equals(leftLimit) || bodyB.equals(leftLimit)) {
						pointLightLeft.setPosition(0, collidePoint.y);
						pointLightLeft.setDistance(height*2/PPM);
						pointLightLeft.setColor(randomColor());
					} else if (bodyA.equals(rightLimit) || bodyB.equals(rightLimit)) {
						pointLightRight.setPosition(width/PPM, collidePoint.y);
						pointLightRight.setDistance(height*2/PPM);
						pointLightRight.setColor(randomColor());
					}
				}
			};

			@Override
			public void endContact (Contact contact) {
					isBallColliding = false;
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub
			};
		});
	}

	@Override
	public void render () {
		
		// time step
		float delta = Gdx.graphics.getDeltaTime();
		world.step(delta, 6, 2);

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		hudCamera.update();
		b2Camera.update();
		rayHandler.updateAndRender();
		pointLightBall.setPosition(ball.getPosition());
//		shapeRenderer.setProjectionMatrix(hudCamera.combined);
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//		shapeRenderer.setColor(Color.GRAY);
//		shapeRenderer.circle(ball.getPosition().x * PPM, ball.getPosition().y * PPM, radius);
//		shapeRenderer.end();
		
		
		world.setGravity(new Vector2(-Gdx.input.getAccelerometerX(), -Gdx.input.getAccelerometerY()));
		ball.applyForceToCenter(Gdx.input.getDeltaX(), -Gdx.input.getDeltaY(), true);
		if (pointLightTop.getDistance() > 0) pointLightTop.setDistance(pointLightTop.getDistance() - delta*7);
		if (pointLightBottom.getDistance() > 0) pointLightBottom.setDistance(pointLightBottom.getDistance() - delta*7);
		if (pointLightLeft.getDistance() > 0) pointLightLeft.setDistance(pointLightLeft.getDistance() - delta*7);
		if (pointLightRight.getDistance() > 0) pointLightRight.setDistance(pointLightRight.getDistance() - delta*7);
	}
	
	private Color randomColor() {
		return new Color((float)Math.random(), (float)Math.random(), (float)Math.random(), 1);
	}
}
