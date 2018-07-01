/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.threed;

/** Class containing methods for converting gradients and Hessian
 * matrices from spherical to Cartesian coordinates.
 */
public class SphericalDerivativeConverter {

    /** Spherical coordinates. */
    private final SphericalCoordinates spherical;

    /** Cartesian vector equivalent to spherical coordinates. */
    private final Vector3D vector;

    /** Jacobian of (r, &theta; &Phi;). */
    private double[][] jacobian;

    /** Hessian of radius. */
    private double[][] rHessian;

    /** Hessian of azimuthal angle in the x-y plane &theta;. */
    private double[][] thetaHessian;

    /** Hessian of polar (co-latitude) angle &Phi;. */
    private double[][] phiHessian;

    public SphericalDerivativeConverter(SphericalCoordinates spherical) {
        this.spherical = spherical;
        this.vector = spherical.toVector();

        computeJacobian();
    }

    /** Return the {@link SphericalCoordinates} for this instance.
     * @return spherical coordinates for this instance
     */
    public SphericalCoordinates getSpherical() {
        return spherical;
    }

    /** Return the {@link Vector3D} for this instance. This vector is
     * equivalent to the spherical coordinates.
     * @return vector for this instance
     */
    public Vector3D getVector() {
        return vector;
    }

    /** Convert a gradient with respect to spherical coordinates into a gradient
     * with respect to Cartesian coordinates.
     * @param sGradient gradient with respect to spherical coordinates
     * {df/dr, df/d&theta;, df/d&Phi;}
     * @return gradient with respect to Cartesian coordinates
     * {df/dx, df/dy, df/dz}
     */
    public double[] toCartesianGradient(final double[] sGradient) {
        // compose derivatives as gradient^T . J
        // the expressions have been simplified since we know jacobian[1][2] = dTheta/dZ = 0
        return new double[] {
            sGradient[0] * jacobian[0][0] + sGradient[1] * jacobian[1][0] + sGradient[2] * jacobian[2][0],
            sGradient[0] * jacobian[0][1] + sGradient[1] * jacobian[1][1] + sGradient[2] * jacobian[2][1],
            sGradient[0] * jacobian[0][2]                                 + sGradient[2] * jacobian[2][2]
        };
    }

    /** Convert a Hessian with respect to spherical coordinates into a Hessian
     * with respect to Cartesian coordinates.
     * <p>
     * As Hessian are always symmetric, we use only the lower left part of the provided
     * spherical Hessian, so the upper part may not be initialized. However, we still
     * do fill up the complete array we create, with guaranteed symmetry.
     * </p>
     * @param sHessian Hessian with respect to spherical coordinates
     * {{d<sup>2</sup>f/dr<sup>2</sup>, d<sup>2</sup>f/drd&theta;, d<sup>2</sup>f/drd&Phi;},
     *  {d<sup>2</sup>f/drd&theta;, d<sup>2</sup>f/d&theta;<sup>2</sup>, d<sup>2</sup>f/d&theta;d&Phi;},
     *  {d<sup>2</sup>f/drd&Phi;, d<sup>2</sup>f/d&theta;d&Phi;, d<sup>2</sup>f/d&Phi;<sup>2</sup>}
     * @param sGradient gradient with respect to spherical coordinates
     * {df/dr, df/d&theta;, df/d&Phi;}
     * @return Hessian with respect to Cartesian coordinates
     * {{d<sup>2</sup>f/dx<sup>2</sup>, d<sup>2</sup>f/dxdy, d<sup>2</sup>f/dxdz},
     *  {d<sup>2</sup>f/dxdy, d<sup>2</sup>f/dy<sup>2</sup>, d<sup>2</sup>f/dydz},
     *  {d<sup>2</sup>f/dxdz, d<sup>2</sup>f/dydz, d<sup>2</sup>f/dz<sup>2</sup>}}
     */
    public double[][] toCartesianHessian(final double[][] sHessian, final double[] sGradient) {
        computeHessians();

        // compose derivative as J^T . H_f . J + df/dr H_r + df/dtheta H_theta + df/dphi H_phi
        // the expressions have been simplified since we know jacobian[1][2] = dTheta/dZ = 0
        // and H_theta is only a 2x2 matrix as it does not depend on z
        final double[][] hj = new double[3][3];
        final double[][] cHessian = new double[3][3];

        // compute H_f . J
        // beware we use ONLY the lower-left part of sHessian
        hj[0][0] = sHessian[0][0] * jacobian[0][0] + sHessian[1][0] * jacobian[1][0] + sHessian[2][0] * jacobian[2][0];
        hj[0][1] = sHessian[0][0] * jacobian[0][1] + sHessian[1][0] * jacobian[1][1] + sHessian[2][0] * jacobian[2][1];
        hj[0][2] = sHessian[0][0] * jacobian[0][2]                                   + sHessian[2][0] * jacobian[2][2];
        hj[1][0] = sHessian[1][0] * jacobian[0][0] + sHessian[1][1] * jacobian[1][0] + sHessian[2][1] * jacobian[2][0];
        hj[1][1] = sHessian[1][0] * jacobian[0][1] + sHessian[1][1] * jacobian[1][1] + sHessian[2][1] * jacobian[2][1];
        // don't compute hj[1][2] as it is not used below
        hj[2][0] = sHessian[2][0] * jacobian[0][0] + sHessian[2][1] * jacobian[1][0] + sHessian[2][2] * jacobian[2][0];
        hj[2][1] = sHessian[2][0] * jacobian[0][1] + sHessian[2][1] * jacobian[1][1] + sHessian[2][2] * jacobian[2][1];
        hj[2][2] = sHessian[2][0] * jacobian[0][2]                                   + sHessian[2][2] * jacobian[2][2];

        // compute lower-left part of J^T . H_f . J
        cHessian[0][0] = jacobian[0][0] * hj[0][0] + jacobian[1][0] * hj[1][0] + jacobian[2][0] * hj[2][0];
        cHessian[1][0] = jacobian[0][1] * hj[0][0] + jacobian[1][1] * hj[1][0] + jacobian[2][1] * hj[2][0];
        cHessian[2][0] = jacobian[0][2] * hj[0][0]                             + jacobian[2][2] * hj[2][0];
        cHessian[1][1] = jacobian[0][1] * hj[0][1] + jacobian[1][1] * hj[1][1] + jacobian[2][1] * hj[2][1];
        cHessian[2][1] = jacobian[0][2] * hj[0][1]                             + jacobian[2][2] * hj[2][1];
        cHessian[2][2] = jacobian[0][2] * hj[0][2]                             + jacobian[2][2] * hj[2][2];

        // add gradient contribution
        cHessian[0][0] += sGradient[0] * rHessian[0][0] + sGradient[1] * thetaHessian[0][0] + sGradient[2] * phiHessian[0][0];
        cHessian[1][0] += sGradient[0] * rHessian[1][0] + sGradient[1] * thetaHessian[1][0] + sGradient[2] * phiHessian[1][0];
        cHessian[2][0] += sGradient[0] * rHessian[2][0]                                     + sGradient[2] * phiHessian[2][0];
        cHessian[1][1] += sGradient[0] * rHessian[1][1] + sGradient[1] * thetaHessian[1][1] + sGradient[2] * phiHessian[1][1];
        cHessian[2][1] += sGradient[0] * rHessian[2][1]                                     + sGradient[2] * phiHessian[2][1];
        cHessian[2][2] += sGradient[0] * rHessian[2][2]                                     + sGradient[2] * phiHessian[2][2];

        // ensure symmetry
        cHessian[0][1] = cHessian[1][0];
        cHessian[0][2] = cHessian[2][0];
        cHessian[1][2] = cHessian[2][1];

        return cHessian;
    }

    /** Evaluates (r, &theta;, &phi;) Jacobian. */
    private void computeJacobian() {

        // intermediate variables
        final double r    = spherical.getRadius();
        final double x    = vector.getX();
        final double y    = vector.getY();
        final double z    = vector.getZ();
        final double rho2 = x * x + y * y;
        final double rho  = Math.sqrt(rho2);
        final double r2   = rho2 + z * z;

        jacobian = new double[3][3];

        // row representing the gradient of r
        jacobian[0][0] = x / r;
        jacobian[0][1] = y / r;
        jacobian[0][2] = z / r;

        // row representing the gradient of theta
        jacobian[1][0] = -y / rho2;
        jacobian[1][1] =  x / rho2;
        // jacobian[1][2] is already set to 0 at allocation time

        // row representing the gradient of phi
        jacobian[2][0] = x * z / (rho * r2);
        jacobian[2][1] = y * z / (rho * r2);
        jacobian[2][2] = -rho / r2;
    }

    /** Lazy evaluation of Hessians. */
    private void computeHessians() {
        if (rHessian == null) {

            // intermediate variables
            final double r      = spherical.getRadius();
            final double x      = vector.getX();
            final double y      = vector.getY();
            final double z      = vector.getZ();
            final double x2     = x * x;
            final double y2     = y * y;
            final double z2     = z * z;
            final double rho2   = x2 + y2;
            final double rho    = Math.sqrt(rho2);
            final double r2     = rho2 + z2;
            final double xOr    = x / r;
            final double yOr    = y / r;
            final double zOr    = z / r;
            final double xOrho2 = x / rho2;
            final double yOrho2 = y / rho2;
            final double xOr3   = xOr / r2;
            final double yOr3   = yOr / r2;
            final double zOr3   = zOr / r2;

            // lower-left part of Hessian of r
            rHessian = new double[3][3];
            rHessian[0][0] = y * yOr3 + z * zOr3;
            rHessian[1][0] = -x * yOr3;
            rHessian[2][0] = -z * xOr3;
            rHessian[1][1] = x * xOr3 + z * zOr3;
            rHessian[2][1] = -y * zOr3;
            rHessian[2][2] = x * xOr3 + y * yOr3;

            // upper-right part is symmetric
            rHessian[0][1] = rHessian[1][0];
            rHessian[0][2] = rHessian[2][0];
            rHessian[1][2] = rHessian[2][1];

            // lower-left part of Hessian of azimuthal angle theta
            thetaHessian = new double[2][2];
            thetaHessian[0][0] = 2 * xOrho2 * yOrho2;
            thetaHessian[1][0] = yOrho2 * yOrho2 - xOrho2 * xOrho2;
            thetaHessian[1][1] = -2 * xOrho2 * yOrho2;

            // upper-right part is symmetric
            thetaHessian[0][1] = thetaHessian[1][0];

            // lower-left part of Hessian of polar (co-latitude) angle phi
            final double rhor2       = rho * r2;
            final double rho2r2      = rho * rhor2;
            final double rhor4       = rhor2 * r2;
            final double rho3r4      = rhor4 * rho2;
            final double r2P2rho2    = 3 * rho2 + z2;
            phiHessian = new double[3][3];
            phiHessian[0][0] = z * (rho2r2 - x2 * r2P2rho2) / rho3r4;
            phiHessian[1][0] = -x * y * z * r2P2rho2 / rho3r4;
            phiHessian[2][0] = x * (rho2 - z2) / rhor4;
            phiHessian[1][1] = z * (rho2r2 - y2 * r2P2rho2) / rho3r4;
            phiHessian[2][1] = y * (rho2 - z2) / rhor4;
            phiHessian[2][2] = 2 * rho * zOr3 / r;

            // upper-right part is symmetric
            phiHessian[0][1] = phiHessian[1][0];
            phiHessian[0][2] = phiHessian[2][0];
            phiHessian[1][2] = phiHessian[2][1];
        }
    }
}
